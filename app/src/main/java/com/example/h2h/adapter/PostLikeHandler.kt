package com.example.h2h.adapter

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import com.example.h2h.R
import com.example.h2h.models.CreatePost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostLikeHandler {

    fun handleLikeClick(post: CreatePost, imageViewLikef: ImageView, likesCountTextView: TextView) {
        val postId = post.postId
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (postId != null && currentUserUid != null) {
            val postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId)
            val likesRef = postRef.child("likes")

            likesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentLikes = snapshot.value as? Map<String, Boolean> ?: emptyMap()
                    val isLiked = currentLikes.containsKey(currentUserUid)

                    if (isLiked) {
                        imageViewLikef.setColorFilter(ContextCompat.getColor(imageViewLikef.context, R.color.color_select_like))
                        likesCountTextView.text = currentLikes.size.toString()
                    } else {
                        imageViewLikef.setColorFilter(ContextCompat.getColor(imageViewLikef.context, R.color.color_unlike))
                        likesCountTextView.text = currentLikes.size.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PostLikeHandler", "Lỗi khi lắng nghe sự thay đổi của likes: ${error.message}")
                }
            })

            // Thêm/bỏ like trong database
            if (!post.likes.containsKey(currentUserUid)) {
                likesRef.child(currentUserUid).setValue(true)
            } else {
                likesRef.child(currentUserUid).removeValue()
            }
        }
    }
}