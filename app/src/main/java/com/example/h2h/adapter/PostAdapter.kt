package com.example.h2h.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.glance.visibility
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.R
import com.example.h2h.models.CreatePost
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(private val posts: List<CreatePost>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Lấy thông tin người dùng từ Firebase
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(post.userId)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(com.example.h2h.models.User::class.java) // Thay thế bằng data class User của bạn
                if (user != null) {
                    holder.userNameTextView.text = user.name
                    Picasso.get().load(user.profileImageUrl).into(holder.userAvatarImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        // Hiển thị nội dung bài đăng
        holder.contentTextView.text = post.content

        // Hiển thị ảnh hoặc video
        if (post.imageOrVideoUrl != null) {
            Picasso.get().load(post.imageOrVideoUrl).into(holder.imagePostImageView)
        } else {
            holder.imagePostImageView.visibility = View.GONE
        }

        // Hiển thị thời gian đăng bài
        holder.timePostTextView.text = formatTimeAgo(post.timestamp)

        // Hiển thị số lượt like, comment, share
        holder.likesCountTextView.text = post.likes.toString()
        holder.commentsCountTextView.text = post.comments.toString()
        holder.sharesCountTextView.text = post.shares.toString()
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.nameView)
        val userAvatarImageView: ImageView = view.findViewById(R.id.avt_image)
        val contentTextView: TextView = view.findViewById(R.id.text_status)
        val imagePostImageView: ImageView = view.findViewById(R.id.image_post)
        val timePostTextView: TextView = view.findViewById(R.id.time_post)
        val likesCountTextView: TextView = view.findViewById(R.id.textViewLikeCountf)
        val commentsCountTextView: TextView = view.findViewById(R.id.textViewCmtCountf)
        val sharesCountTextView: TextView = view.findViewById(R.id.textViewShareCountf)
    }

    // Hàm định dạng thời gian đăng bài
    private fun formatTimeAgo(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 3 -> "$days ngày trước"
            else -> {
                val date = Date(timestamp)
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                format.format(date)
            }
        }
    }
}