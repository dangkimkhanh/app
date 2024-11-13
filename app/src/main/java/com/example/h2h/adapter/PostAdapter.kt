package com.example.h2h.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.semantics.text
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.glance.visibility
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.R
import com.example.h2h.models.CreatePost
import com.example.h2h.models.User
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class PostAdapter(private val posts: List<CreatePost>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private val postLikeHandler = PostLikeHandler()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.likePostImageView.setOnClickListener {
            postLikeHandler.handleLikeClick(post, holder.likePostImageView, holder.likesCountTextView)
        }
        val isLiked = post.likes.containsKey(currentUserUid) // Kiểm tra xem người dùng hiện tại đã like bài viết chưa
        if (isLiked) {
            holder.likePostImageView.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.color_select_like)) // Đổi màu tim sang đỏ
        } else {
            holder.likePostImageView.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.color_unlike)) // Đổi màu tim sang xám
        }
        holder.editPostImageView.setOnClickListener {
            val popupMenu = PopupMenu(holder.itemView.context, holder.editPostImageView)
            popupMenu.inflate(R.menu.my_popup_menu) // Sử dụng file XML menu bạn đã tạo

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        // Gọi hàm hiển thị dialog sửa bài viết
                        showEditDialog(holder.itemView.context, post)
                        true
                    }
                    R.id.action_delete -> {
                        // Gọi hàm xóa bài viết khỏi Firebase
                        deletePostFromFirebase(post, holder)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
        Log.d("PostAdapter", "Binding post at position $position: $post")
        // Load user info from Firebase
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(post.userId ?: "")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("PostAdapter", "User data loaded for post: $post")
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    holder.userNameTextView.text = user.name
                    Picasso.get().load(user.profileImageUrl).into(holder.userAvatarImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Display post content
        holder.contentTextView.text = post.content
        if (post.imageOrVideoUrl != null) {
            Picasso.get().load(post.imageOrVideoUrl).into(holder.imagePostImageView)
            holder.imagePostImageView.visibility = View.VISIBLE
        } else {
            holder.imagePostImageView.visibility = View.GONE
        }
        val likesCount = if (post.likes.isEmpty()) {
            0
        } else {
            post.likes.size
        }
        val commentsCount = if(post.comments.isEmpty()){
            0
        }else{
            post.comments.size
        }
        val sharesCount = if(post.shares.isEmpty()){
            0
        }else{
            post.shares.size
        }
        holder.timePostTextView.text = formatTimeAgo(post.timestamp ?: 0L)
        holder.likesCountTextView.text = likesCount.toString()
        holder.commentsCountTextView.text = commentsCount.toString()
        holder.sharesCountTextView.text = sharesCount.toString()
    }

    override fun getItemCount(): Int = posts.size

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.nameView)
        val userAvatarImageView: ImageView = view.findViewById(R.id.avt_image)
        val contentTextView: TextView = view.findViewById(R.id.text_status)
        val imagePostImageView: ImageView = view.findViewById(R.id.image_post)
        val timePostTextView: TextView = view.findViewById(R.id.time_post)
        val likesCountTextView: TextView = view.findViewById(R.id.textViewLikeCountf)
        val commentsCountTextView: TextView = view.findViewById(R.id.textViewCmtCountf)
        val sharesCountTextView: TextView = view.findViewById(R.id.textViewShareCountf)
        val editPostImageView: ImageView = view.findViewById(R.id.edit_post)
        val likePostImageView: ImageView = view.findViewById(R.id.imageViewLikef)
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
    private fun showEditDialog(context: Context, post: CreatePost) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sửa nội dung")

        // Tạo EditText để nhập nội dung mới
        val input = EditText(context)
        input.setText(post.content)
        builder.setView(input)

        builder.setPositiveButton("Lưu") { dialog, _ ->
            val newContent = input.text.toString()
            updatePostContentInFirebase(post, newContent)
            dialog.dismiss()
        }
        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun updatePostContentInFirebase(post: CreatePost, newContent: String) {
        val postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.postId ?: "")
        postRef.child("content").setValue(newContent)
    }

    // ... (Các code khác)

    private fun deletePostFromFirebase(post: CreatePost, holder: PostViewHolder) {
        // Tạo AlertDialog để xác nhận xóa
        val builder = AlertDialog.Builder(holder.itemView.context) // Sử dụng context của itemView
        builder.setTitle("Xác nhận xóa")
        builder.setMessage("Bạn có chắc chắn muốn xóa bài viết này?")

        builder.setPositiveButton("Xóa") { dialog, _ ->
            // Thực hiện xóa bài viết khỏi Firebase
            val postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.postId ?: "")
            postRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(holder.itemView.context, "Đã xóa bài viết", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("PostAdapter", "Lỗi khi xóa bài viết: ${task.exception}")
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

// ... (Các code khác)

}