package com.example.h2h.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.h2h.R
import com.example.h2h.Post

class PostAdapter(private val postList: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.nameView)
        val postTime: TextView = itemView.findViewById(R.id.postTime)
        val postContent: TextView = itemView.findViewById(R.id.postContent)
        val likeCount: TextView = itemView.findViewById(R.id.textViewLikeCountf)
        val commentCount: TextView = itemView.findViewById(R.id.textViewCmtCountf)
        val shareCount: TextView = itemView.findViewById(R.id.textViewShareCountf)
        val avatar: ImageView = itemView.findViewById(R.id.avatarImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.userName.text = post.userName
        holder.postTime.text = post.postTime
        holder.postContent.text = post.content
        holder.likeCount.text = post.likeCount.toString()
        holder.commentCount.text = post.commentCount.toString()
        holder.shareCount.text = post.shareCount.toString()

        // Avatar mặc định (thay thế bằng ảnh khác nếu cần)
        holder.avatar.setImageResource(R.drawable.ic_avatar)
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}
