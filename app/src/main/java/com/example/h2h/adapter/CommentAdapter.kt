package com.example.h2h.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.Activity.OnReplyClickListener
import com.example.h2h.R
import com.example.h2h.models.Comment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(private val context: Context, private val onReplyClickListener: OnReplyClickListener, private val postId: String) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val comments: MutableList<Comment> = mutableListOf()

    init {
        loadComments(postId)
    }

    private fun loadComments(postId: String) {
        val commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                comments.clear()
                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(Comment::class.java)
                    if (comment != null && comment.parentId == null) { // Chỉ lấy comment cấp 1
                        comments.add(comment)
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi khi lấy dữ liệu comment
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_parent, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.replyButton.setOnClickListener {
            Log.d("CommentAdapter", "Reply button clicked")
            onReplyClickListener.showReplyInput(comment)
        }
        // Lấy thông tin người dùng từ Firebase
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(comment.userId!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(com.example.h2h.models.User::class.java)
                if (user != null) {
                    // Hiển thị tên và ảnh avatar của người dùng
                    holder.usernameTextView.text = user.name
                    Picasso.get().load(user.profileImageUrl).into(holder.avatarImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi khi lấy dữ liệu người dùng
            }
        })

        // Hiển thị nội dung và thời gian comment
        holder.contentTextView.text = comment.content
        holder.timeTextView.text = formatTimeAgo(comment.timestamp ?: 0L)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatarImageView: ImageView = itemView.findViewById(R.id.comment_avatar)
        val usernameTextView: TextView = itemView.findViewById(R.id.comment_username)
        val contentTextView: TextView = itemView.findViewById(R.id.comment_content)
        val timeTextView: TextView = itemView.findViewById(R.id.comment_time)
        val replyButton: TextView = itemView.findViewById(R.id.reply_button)
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
}