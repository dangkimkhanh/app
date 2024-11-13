package com.example.h2h

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.h2h.models.CreatePost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

@Suppress("DEPRECATION")
class CreatePostActivity : AppCompatActivity() {

    private lateinit var mediaUri: Uri
    private lateinit var imagePost: ImageView
    private val PICK_MEDIA_REQUEST = 1
    private var isVideo = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        val back = findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener {
            finish()
        }
        val selectImage = findViewById<LinearLayout>(R.id.select_image)
        imagePost = findViewById(R.id.image_post)
        val confirmPost = findViewById<Button>(R.id.confirm_post)
        val textStatus = findViewById<EditText>(R.id.text_status)

        // Khi bấm vào thêm ảnh hoặc video
        selectImage.setOnClickListener {
            openMediaChooser()
        }

        // Khi bấm nút đăng bài
        confirmPost.setOnClickListener {
            val content = textStatus.text.toString().trim()
            if (content.isEmpty() && !::mediaUri.isInitialized) {
                Toast.makeText(this, "Vui lòng thêm nội dung hoặc chọn ảnh/video", Toast.LENGTH_SHORT).show()
            } else {
                if (::mediaUri.isInitialized) {
                    uploadMediaAndPost(content)
                } else {
                    postToFirebaseDatabase(content, null)
                }
            }
        }
        val usernameTextView = findViewById<TextView>(R.id.username)
        val userAvatarImageView = findViewById<ImageView>(R.id.view_avatar)

        // Truy vấn dữ liệu người dùng
        val userId = Firebase.auth.currentUser!!.uid
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val user = snapshot.getValue(com.example.h2h.models.User::class.java) // Sử dụng data class User của bạn
            if (user != null) {
                // Hiển thị dữ liệu lên view
                usernameTextView.text = user.name
                Picasso.get().load(user.profileImageUrl).into(userAvatarImageView)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // Xử lý lỗi
            Log.e("CreatePostActivity", "Lỗi khi lấy thông tin người dùng", error.toException())
        }
    })
    }
    // Trong onCreate() hoặc một hàm khác
    val userId = Firebase.auth.currentUser!!.uid
    val usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
    

    private fun openMediaChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*,video/*") // Sử dụng setDataAndType()
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }
    @Deprecated("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            mediaUri = data.data!!

            // Kiểm tra xem người dùng chọn ảnh hay video
            val mimeType = contentResolver.getType(mediaUri)
            isVideo = mimeType?.startsWith("video") == true

            if (isVideo) {
                // Hiển thị biểu tượng video nếu là video
                imagePost.setImageResource(R.drawable.ic_video_img_view)
            } else {
                // Hiển thị ảnh nếu là ảnh
                imagePost.setImageURI(mediaUri)
            }
        }
    }

    private fun uploadMediaAndPost(content: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("posts/${UUID.randomUUID()}")
        storageRef.putFile(mediaUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    postToFirebaseDatabase(content, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload media", Toast.LENGTH_SHORT).show()
            }
    }

    private fun postToFirebaseDatabase(content: String, mediaUrl: String?) {
        val postsRef = FirebaseDatabase.getInstance().getReference("posts")
        val newPostRef = postsRef.push()

        val userId = Firebase.auth.currentUser!!.uid
        val newPost = CreatePost(
            postId = newPostRef.key,
            content = content,
            imageOrVideoUrl = mediaUrl,
            timestamp = System.currentTimeMillis(),
            userId = userId ,
            likes = emptyMap(), // Khởi tạo likes là một map rỗng
            comments = emptyMap(), // Khởi tạo comments là một map rỗng
            shares = emptyMap() // Khởi tạo shares là một map rỗng
        )

        newPostRef.setValue(newPost)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã tạo bài đăng mới!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                // Ghi log chi tiết lỗi vào Logcat
                Log.e("CreatePostActivity", "Error creating post", exception)

                // Hiển thị thông báo chi tiết cho người dùng
                Toast.makeText(this, " ${exception.message}", Toast.LENGTH_LONG)
                    .show()
            }

    }

}