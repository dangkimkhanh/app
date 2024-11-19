package com.example.h2h

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.request.transition.Transition
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.example.h2h.adapter.MessageAdapter
import com.example.h2h.models.Message
import com.example.h2h.models.MessageContent
import com.example.h2h.models.User
import com.example.h2h.models.UserChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.text.format
import kotlin.text.toByteArray

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {
    private lateinit var username: TextView
    private lateinit var avatarImage: ImageView
    private lateinit var usernametimeon: TextView
    private lateinit var inputChat: EditText
    private lateinit var sendMessage: ImageView
    private lateinit var sendImage: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messages: MutableList<Message>
    private val REQUEST_IMAGE_PICK = 1
    private var currentUserId: String? = null
    private var otherUserId: String? = null
    private var isLoading = false
    private var lastVisibleMessageKey: String? = null
    private val messagesToLoad = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        username = findViewById(R.id.username)
        avatarImage = findViewById(R.id.avatar_image)
        usernametimeon = findViewById(R.id.usernametimeon)
        setSupportActionBar(findViewById(R.id.toolbar_chat))
        inputChat = findViewById(R.id.input_chat)
        sendMessage = findViewById(R.id.send_message)
        sendImage = findViewById(R.id.chose_send_image)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid
        otherUserId = intent.getStringExtra("OTHER_USER_ID")

        val currentUserIdCopy = currentUserId // Bản sao cục bộ
        val otherUserIdCopy = otherUserId // Bản sao cục bộ
        messageRecyclerView = findViewById(R.id.message_recycler_view)
        messageRecyclerView.layoutManager = LinearLayoutManager(this)

        messages = mutableListOf()
        messageAdapter = MessageAdapter(this, messages)
        messageRecyclerView.adapter = messageAdapter

        // Initial message loading
        loadMessages()

        // Add scroll listener for pagination
        messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (firstVisibleItemPosition == 0 && !isLoading) {
                    loadMessages(lastVisibleMessageKey)
                }
            }
        })

        if (otherUserIdCopy != null && currentUserIdCopy != null) {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(otherUserIdCopy)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        username.text = user.name
                        Glide.with(this@ChatActivity).load(user.profileImageUrl).into(avatarImage)

                        val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(otherUserIdCopy)
                        presenceRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val status = snapshot.getValue(String::class.java)
                                usernametimeon.text = if (status == "online") "Đang hoạt động" else "Hoạt động gần đây"
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Xử lý lỗi
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi
                }
            })

            sendMessage.setOnClickListener {
                val messageText = inputChat.text.toString()
                if (messageText.isNotEmpty()) {
                    val messageContent = MessageContent(text = messageText)
                    val message = Message(
                        senderId = currentUserIdCopy,
                        receiverId = otherUserIdCopy,
                        type = "text",
                        content = messageContent,
                        timestamp = System.currentTimeMillis()
                    )

                    val chatId = generateChatId(currentUserIdCopy, otherUserIdCopy)
                    val userChatsRef = FirebaseDatabase.getInstance().reference.child("userChats")

                    userChatsRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                sendMessage(chatId, message)
                            } else {
                                createChat(chatId, message)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Xử lý lỗi
                        }
                    })

                    inputChat.text.clear()
                }
            }

            sendImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_IMAGE_PICK)
            }
        }
    }

    private fun loadMessages(startAfterKey: String? = null) {
        isLoading = true

        val chatId = generateChatId(currentUserId!!, otherUserId!!)
        val messagesRef = FirebaseDatabase.getInstance().reference.child("messages").child(chatId)
            .orderByKey()
            .limitToLast(messagesToLoad)

        if (startAfterKey != null) {
            messagesRef.startAfter(startAfterKey)
        }

        // Sử dụng addChildEventListener thay vì addListenerForSingleValueEvent
        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null && !messages.contains(message)) { // Kiểm tra tin nhắn đã tồn tại chưa
                    messages.add(message)
                    messageAdapter.notifyItemInserted(messages.size - 1)
                    messageRecyclerView.scrollToPosition(messages.size - 1)
                }
                isLoading = false
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Xử lý khi tin nhắn được thay đổi (nếu cần)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Xử lý khi tin nhắn bị xóa (nếu cần)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Xử lý khi tin nhắn được di chuyển (nếu cần)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
                isLoading = false
            }
        })
    }

    private fun createChat(chatId: String, message: Message) {
        val userChatsRef = FirebaseDatabase.getInstance().reference.child("userChats")
        val messagesRef = FirebaseDatabase.getInstance().reference.child("messages")

        val userChat = UserChat(lastMessage = message, chatId = chatId)

        userChatsRef.child(chatId).setValue(userChat)
        messagesRef.child(chatId).push().setValue(message)
    }

    private fun sendMessage(chatId: String, message: Message) {
        val messagesRef = FirebaseDatabase.getInstance().reference.child("messages").child(chatId)
        messagesRef.push().setValue(message)

        // Cập nhật danh sách messages và adapter sau khi gửi tin nhắn thành công
        messages.add(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        messageRecyclerView.scrollToPosition(messages.size - 1)

        // Cập nhật lastMessage trong userChats/chatId
        val userChatsRef = FirebaseDatabase.getInstance().reference.child("userChats").child(chatId)
        userChatsRef.child("lastMessage").setValue(message) // Chỉ cập nhật lastMessage trong node chatId
    }

    private fun generateChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "${userId1}_$userId2"
        } else {
            "${userId2}_$userId1"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                uploadImageAndSendMessage(imageUri)
            }
        }
    }

    private fun uploadImageAndSendMessage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")

        // Nén ảnh bằng Glide
        Glide.with(this)
            .asBitmap()
            .load(imageUri)
            .apply(
                RequestOptions()
                    .override(1024, 768) // Thay đổi kích thước ảnh nếu cần
                    .format(DecodeFormat.PREFER_RGB_565) // Định dạng RGB_565
                    .encodeQuality(80) // Chất lượng nén 80% (điều chỉnh theo nhu cầu)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Tắt cache đĩa
                    .skipMemoryCache(true) // Tắt cache bộ nhớ
            )
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    val baos =
                        ByteArrayOutputStream()
                    resource.compress(Bitmap.CompressFormat.JPEG, 80, baos) // Nén ảnh
                    val data = baos.toByteArray()

                    // Tải ảnh đã nén lên Firebase Storage
                    imageRef.putBytes(data)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()

                                val messageContent = MessageContent(imageUrl = imageUrl)
                                val message = Message(
                                    senderId = currentUserId,
                                    receiverId = otherUserId,
                                    type = "image",
                                    content = messageContent,
                                    timestamp = System.currentTimeMillis()
                                )

                                val chatId = generateChatId(currentUserId!!, otherUserId!!)
                                sendMessage(chatId, message)
                            }
                        }
                        .addOnFailureListener {
                            // Xử lý lỗi upload
                        }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Xử lý khi tải ảnh bị hủy
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
