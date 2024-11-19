package com.example.h2h.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.glance.visibility
import com.example.h2h.R
import com.example.h2h.models.Comment
import com.example.h2h.models.CreatePost
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.adapter.CommentAdapter
import kotlin.text.clear
import kotlin.text.isNotEmpty
import kotlin.text.trim

interface OnReplyClickListener {
    fun showReplyInput(comment: Comment)
}
@Suppress("DEPRECATION")
class ViewPostActivity : AppCompatActivity(), OnReplyClickListener {
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var nameView: TextView
    private lateinit var timePostView: TextView
    private lateinit var contentView: TextView
    private lateinit var imagePostImageView: ImageView
    private lateinit var likesCountTextView: TextView
    private lateinit var commentsCountTextView: TextView
    private lateinit var sharesCountTextView: TextView
    private lateinit var userAvatarImageView: ImageView
    private lateinit var likeStatusImageView: ImageView
    private lateinit var inputComment: EditText
    private lateinit var sendComment: ImageView
    private var replyingToCommentId: String? = null
    private var replyingToUserId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_post)
        // Ánh xạ các View
        nameView = findViewById(R.id.nameView_user)
        timePostView = findViewById(R.id.time_post_user)
        contentView = findViewById(R.id.text_status_user)
        imagePostImageView = findViewById(R.id.image_post_user)
        likesCountTextView = findViewById(R.id.textViewLikeCountf)
        commentsCountTextView = findViewById(R.id.textViewCmtCountf)
        sharesCountTextView = findViewById(R.id.textViewShareCountf)
        userAvatarImageView = findViewById(R.id.avt_image_user)
        likeStatusImageView = findViewById(R.id.imageViewLikef)
        inputComment = findViewById(R.id.input_comment)
        sendComment = findViewById(R.id.send_comment)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_view_post)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val postIdThis = intent.getStringExtra("postId")
        val commentRecyclerView = findViewById<RecyclerView>(R.id.comment_recycler_view)
        val commentAdapter = CommentAdapter(this, this, postIdThis!!) // Truyền postId vào CommentAdapter
        commentRecyclerView.adapter = commentAdapter
        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        val commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postIdThis)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentsCount = snapshot.childrenCount.toInt()
                commentsCountTextView.text = commentsCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })
        sendComment.setOnClickListener {
            val commentContent = inputComment.text.toString().trim()

            if (commentContent.isNotEmpty()) {
                val postId = intent.getStringExtra("postId")
                val userId = currentUserUid

                if (postId != null && userId != null) {
                    val commentRef = if (replyingToCommentId != null) {
                        FirebaseDatabase.getInstance().getReference("comments").child(postId).child(replyingToCommentId!!).child("replies").push() // Lưu vào replies của comment cha
                    } else {
                        FirebaseDatabase.getInstance().getReference("comments").child(postId).push() // Lưu vào comments (cấp 1)
                    }
                    val commentId = commentRef.key

                    val comment = Comment(
                        parentId = replyingToCommentId,
                        id = commentId,
                        postId = postId,
                        content = commentContent,
                        userId = userId,
                        timestamp = System.currentTimeMillis()
                    )
                    commentRef.setValue(comment)
                        .addOnSuccessListener {
                            // Xóa nội dung trong inputComment và tắt bàn phím
                            inputComment.text.clear()
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(inputComment.windowToken, 0)
                            // Reset replyingToCommentId sau khi gửi comment
                            replyingToCommentId = null
                            inputComment.hint = "Thêm bình luận..." // Đặt lại hint
                        }
                        .addOnFailureListener { e ->
                            Log.e("ViewPostActivity", "Lỗi khi lưu bình luận: ${e.message}")
                        }
                } else {
                    Log.e("ViewPostActivity", "postId or userId is null")
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_view_pos)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val postId = intent.getStringExtra("postId")
        val userId = intent.getStringExtra("userId")

        if (postId != null && userId != null) {
            val postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId)
            postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue(CreatePost::class.java)
                    if (post != null) {
                        val isLiked = post.likes.containsKey(currentUserUid) // Kiểm tra trạng thái like
                        if (isLiked) {
                            likeStatusImageView.setColorFilter(ContextCompat.getColor(this@ViewPostActivity, R.color.color_select_like))
                        } else {
                            likeStatusImageView.setColorFilter(ContextCompat.getColor(this@ViewPostActivity, R.color.color_unlike))
                        }
                        timePostView.text = formatTimeAgo(post.timestamp ?: 0L)
                        contentView.text = post.content

                        if (post.imageOrVideoUrl != null) {
                            Picasso.get().load(post.imageOrVideoUrl).into(imagePostImageView)
                            imagePostImageView.visibility = View.VISIBLE
                        } else {
                            imagePostImageView.visibility = View.GONE
                        }

                        val likesCount = post.likes.size
                        val sharesCount = post.shares.size

                        likesCountTextView.text = likesCount.toString()
                        sharesCountTextView.text = sharesCount.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewPostActivity", "Lỗi khi lấy dữ liệu bài viết: ${error.toException()}")
                }
            })

            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        // Hiển thị tên và ảnh avatar của người dùng
                        nameView.text = user.name
                        Picasso.get().load(user.profileImageUrl).into(userAvatarImageView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewPostActivity", "Lỗi khi lấy dữ liệu người dùng: ${error.toException()}")
                }
            })
        } else {
            Log.e("ViewPostActivity", "postId or userId is null")
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun formatTimeAgo(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        val minutes = diff / 1000 / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 3 -> "$days ngày trước"
            else -> {
                val date = Date(timestamp)
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            }
        }
    }
    override fun showReplyInput(comment: Comment) {
        Log.d("ViewPostActivity", "showReplyInput called")
        inputComment.visibility = View.VISIBLE
        inputComment.requestFocus()
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(comment.userId!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    replyingToUserId = user.name
                    inputComment.hint = "Trả lời cho ${user.name}:"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        replyingToCommentId = if (comment.parentId == null) {
            comment.id
        } else {
            comment.parentId
        }
        inputComment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().startsWith("Trả lời cho $replyingToUserId:")) {
                    // Người dùng chưa xóa tên người được trả lời
                } else {
                    // Người dùng đã xóa tên người được trả lời
                    replyingToCommentId = null // Trở thành comment cấp 1
                    inputComment.hint = "Thêm bình luận..." // Đặt lại hint
                }
            }
        })
    }

}



