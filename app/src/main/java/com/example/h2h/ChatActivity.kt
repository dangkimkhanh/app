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
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.h2h.adapter.MessageAdapter
import com.example.h2h.models.Message
import com.example.h2h.models.MessageContent
import com.example.h2h.models.User
import com.example.h2h.models.UserChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {
    private lateinit var username: TextView
    private lateinit var avatarImage: ImageView
    private lateinit var usernametimeon: TextView
    private lateinit var inputChat: EditText
    private lateinit var sendMessage: ImageView
    private lateinit var sendImage: ImageView
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messages: MutableList<Message>
    private lateinit var viewProfile: Toolbar
    private lateinit var auth: FirebaseAuth
    private var currentUserId: String? = null
    private var otherUserId: String? = null
    private val REQUEST_IMAGE_PICK = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setupEdgeToEdge()

        // Initialize views
        username = findViewById(R.id.username)
        avatarImage = findViewById(R.id.avatar_image)
        usernametimeon = findViewById(R.id.usernametimeon)
        inputChat = findViewById(R.id.input_chat)
        sendMessage = findViewById(R.id.send_message)
        sendImage = findViewById(R.id.chose_send_image)
        viewProfile = findViewById(R.id.toolbar_chat)
        messageRecyclerView = findViewById(R.id.message_recycler_view)

        // Setup Firebase and current user
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid
        otherUserId = intent.getStringExtra("OTHER_USER_ID")

        // Initialize RecyclerView
        messages = mutableListOf()
        messageAdapter = MessageAdapter(this, messages)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true // Tin nhắn mới nhất sẽ ở cuối danh sách
        messageRecyclerView.layoutManager = layoutManager
        messageRecyclerView.adapter = messageAdapter

        setupToolbar()
        loadMessages() // Load và cuộn đến tin nhắn mới nhất
        viewProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("OTHER_USER_ID", otherUserId)
            startActivity(intent)
        }
        // Handle message sending
        sendMessage.setOnClickListener { sendTextMessage() }
        sendImage.setOnClickListener { pickImage() }

        // Load user info
        otherUserId?.let { loadUserInfo(it) }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar_chat))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun loadMessages() {
        val chatId = generateChatId(currentUserId!!, otherUserId!!)
        val messagesRef = FirebaseDatabase.getInstance().reference.child("messages").child(chatId)
            .orderByKey()

        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null && !messages.contains(message)) {
                    messages.add(message)
                    messageAdapter.notifyItemInserted(messages.size - 1)
                    messageRecyclerView.scrollToPosition(messages.size - 1) // Cuộn tới cuối
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadUserInfo(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    username.text = it.name
                    Glide.with(this@ChatActivity).load(it.profileImageUrl).into(avatarImage)
                    loadUserPresence(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadUserPresence(userId: String) {
        val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(userId)
        presenceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                usernametimeon.text =
                    if (status == "online") "Đang hoạt động" else "Hoạt động gần đây"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendTextMessage() {
        val messageText = inputChat.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val chatId = generateChatId(currentUserId!!, otherUserId!!)
            val messageContent = MessageContent(text = messageText)
            val message = Message(
                senderId = currentUserId,
                receiverId = otherUserId,
                type = "text",
                content = messageContent,
                timestamp = System.currentTimeMillis()
            )

            val messagesRef =
                FirebaseDatabase.getInstance().reference.child("messages").child(chatId)
            messagesRef.push().setValue(message).addOnSuccessListener {
                inputChat.text.clear()
                messageRecyclerView.scrollToPosition(messages.size - 1) // Cuộn tới cuối
            }
        }
    }

    private fun pickImage() {
        inputChat.clearFocus()
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun generateChatId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_$userId2" else "${userId2}_$userId1"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data // Lấy Uri của ảnh đã chọn
            sendImage.setImageResource(R.drawable.ic_loading)
            data.data?.let { uploadImageAndSendMessage(it) }
        }
    }

    private fun uploadImageAndSendMessage(imageUri: Uri) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}")
        Glide.with(this).asBitmap().load(imageUri).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                val baos = ByteArrayOutputStream()
                resource.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val data = baos.toByteArray()

                storageRef.putBytes(data).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val messageContent = MessageContent(imageUrl = uri.toString())
                        val message = Message(
                            senderId = currentUserId,
                            receiverId = otherUserId,
                            type = "image",
                            content = messageContent,
                            timestamp = System.currentTimeMillis()
                        )
                        val chatId = generateChatId(currentUserId!!, otherUserId!!)
                        FirebaseDatabase.getInstance().reference.child("messages").child(chatId)
                            .push().setValue(message).addOnSuccessListener {
                                inputChat.requestFocus() // Thêm dòng này
                            }
                        sendImage.setImageResource(R.drawable.ic_image)
                    }
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
