package com.example.h2h.adapter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.ui.draw.paint
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.compose.material.placeholder
import com.bumptech.glide.Glide
import com.example.h2h.ChatActivity
import com.example.h2h.ProfileActivity
import com.example.h2h.R
import com.example.h2h.UserProfileActivity
import com.example.h2h.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import pl.droidsonroids.gif.GifImageView


class ListUserAdapter : RecyclerView.Adapter<ListUserAdapter.UserViewHolder>() {

    private val userList = mutableListOf<User>()
    private var isLoading = false
    private var lastVisibleKey: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUserList(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.image_avatar)
        private val nameTextView: TextView = itemView.findViewById(R.id.text_name)
        private val introductionTextView: TextView = itemView.findViewById(R.id.text_last_seen)
        private val onlineStatusView: GifImageView = itemView.findViewById(R.id.online_status)

        fun bind(user: User) {
            nameTextView.text = user.name
            introductionTextView.text = user.introduction
            itemView.setOnClickListener {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (user.uid == currentUserId) {
                    // Chuyển đến ProfileActivity
                    val intent = Intent(itemView.context, ProfileActivity::class.java)
                    itemView.context.startActivity(intent)
                } else {
                    // Chuyển đến UserProfileActivity
                    val intent = Intent(itemView.context, UserProfileActivity::class.java)
                    intent.putExtra("OTHER_USER_ID", user.uid)
                    itemView.context.startActivity(intent)
                }
            }
            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.ic_avatar)
                .into(avatarImageView)
            val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(user.uid)
            presenceRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status =
                        snapshot.getValue(String::class.java) // Lấy giá trị của presenceRef

                    val shape = ShapeDrawable(OvalShape())
                    shape.paint.color = ContextCompat.getColor(
                        itemView.context,
                        if (status == "online") R.color.green else R.color.gray // Gán màu dựa trên status
                    )
                    onlineStatusView.background = shape // Set ShapeDrawable làm background
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })

        }

    }

    fun loadMoreUsers() {
        if (isLoading || lastVisibleKey == null) {
            return
        }

        isLoading = true

        val usersRef = FirebaseDatabase.getInstance().reference.child("users")
        val query = usersRef.orderByKey().startAfter(lastVisibleKey).limitToFirst(10)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newUsers = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let { newUsers.add(it) }
                }
                if (newUsers.isNotEmpty()) {
                    lastVisibleKey = newUsers.last().uid // Cập nhật lastVisibleKey
                    userList.addAll(newUsers)
                    notifyItemRangeInserted(userList.size - newUsers.size, newUsers.size)
                } else {
                    lastVisibleKey = null // Không còn dữ liệu để tải
                }

                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                // Xử lý lỗi
            }
        })
    }
}