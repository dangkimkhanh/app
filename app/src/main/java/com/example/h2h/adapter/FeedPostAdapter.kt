package com.example.h2h.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.glance.visibility
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.Activity.ViewPostActivity
import com.example.h2h.R
import com.example.h2h.models.CreatePost
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class FeedPostAdapter(private val context: Context) : RecyclerView.Adapter<FeedPostAdapter.PostViewHolder>() {

    private val posts: MutableList<CreatePost> = mutableListOf()
    private val loadedPostIds = mutableSetOf<String>()
    private val postLikeHandler = PostLikeHandler()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    var isLoading = false

    init {
        loadInitialPosts()
    }

    private fun loadInitialPosts() {
        val postsRef = FirebaseDatabase.getInstance().getReference("posts")
        postsRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val randomPosts = snapshot.children.shuffled().take(10)
                for (postSnapshot in randomPosts) {
                    val post = postSnapshot.getValue(CreatePost::class.java)
                    if (post != null && post.postId != null && !loadedPostIds.contains(post.postId)) {
                        posts.add(post)
                        loadedPostIds.add(post.postId!!)
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedPostAdapter", "Error loading initial posts: ${error.message}")
            }
        })
    }

    fun loadMorePosts() {
        if (isLoading) return

        isLoading = true

        val lastPostId = loadedPostIds.lastOrNull()
        if (lastPostId != null) {
            val postsRef = FirebaseDatabase.getInstance().getReference("posts")
            postsRef.orderByChild("timestamp").endBefore(lastPostId).limitToLast(10).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newPosts = snapshot.children.mapNotNull { it.getValue(CreatePost::class.java) }
                        .filter { !loadedPostIds.contains(it.postId) }

                    posts.addAll(0, newPosts)
                    loadedPostIds.addAll(newPosts.map { it.postId!! })

                    notifyDataSetChanged()

                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FeedPostAdapter", "Error loading more posts: ${error.message}")
                    isLoading = false
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val postId = post.postId
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Load user info from Firebase
        val usersRef = FirebaseDatabase.getInstance().getReference("users").child(post.userId ?: "")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    holder.userNameTextView.text = user.name
                    Picasso.get().load(user.profileImageUrl).into(holder.userAvatarImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedPostAdapter", "Error loading user info: ${error.message}")
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

        holder.timePostTextView.text = formatTimeAgo(post.timestamp ?: 0L)

        // Handle likes
        if (postId != null && currentUserUid != null) {
            val postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId)
            val likesRef = postRef.child("likes")

            // Lắng nghe sự thay đổi của likes
            likesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentLikes = snapshot.value as? Map<String, Boolean> ?: emptyMap()
                    val isLiked = currentLikes.containsKey(currentUserUid)

                    // Cập nhật giao diện người dùng
                    if (isLiked) {
                        holder.likePostImageView.setColorFilter(ContextCompat.getColor(context, R.color.color_select_like))
                    } else {
                        holder.likePostImageView.setColorFilter(ContextCompat.getColor(context, R.color.color_unlike))
                    }
                    holder.likesCountTextView.text = currentLikes.size.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FeedPostAdapter", "Lỗi khi lắng nghe sự thay đổi của likes: ${error.message}")
                }
            })

            // Xử lý sự kiện click vào viewLike
            holder.viewLike.setOnClickListener {
                postLikeHandler.handleLikeClick(post, holder.likePostImageView, holder.likesCountTextView)
            }
        }

        // Handle comments
        val commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(post.postId!!)
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentsCount = snapshot.childrenCount.toInt()
                holder.commentsCountTextView.text = commentsCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedPostAdapter", "Error loading comments count: ${error.message}")
            }
        })

        // Handle shares
        holder.sharesCountTextView.text = post.shares.size.toString()

        // Handle show comment
        holder.showComment.setOnClickListener {
            val intent = Intent(context, ViewPostActivity::class.java)
            intent.putExtra("postId", post.postId)
            intent.putExtra("userId", post.userId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.nameView)
        val userAvatarImageView: ImageView = view.findViewById(R.id.avt_image)
        val contentTextView: TextView = view.findViewById(R.id.text_status)
        val imagePostImageView: ImageView = view.findViewById(R.id.image_post)
        val timePostTextView: TextView = view.findViewById(R.id.time_post)
        val likesCountTextView: TextView = view.findViewById(R.id.textViewLikeCountf)
        val commentsCountTextView: TextView = view.findViewById(R.id.textViewCmtCountf)
        val sharesCountTextView: TextView = view.findViewById(R.id.textViewShareCountf)
        val likePostImageView: ImageView = view.findViewById(R.id.imageViewLikef)
        val showComment: LinearLayout = view.findViewById(R.id.comment_post)
        val viewLike: LinearLayout = view.findViewById(R.id.view_like)
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            else -> {
                val date = Date(timestamp)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    }


}


