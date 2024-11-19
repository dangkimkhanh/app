package com.example.h2h.adapter

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.ui.draw.paint
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.h2h.R
import com.example.h2h.models.User
import com.example.h2h.models.UserChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import pl.droidsonroids.gif.GifImageView

class ListItemChatAdapter(
    private val context: Context,
    private val userChats: List<UserChat>,
    private val onItemClickListener: (UserChat) -> Unit
) : RecyclerView.Adapter<ListItemChatAdapter.UserChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false)
        return UserChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserChatViewHolder, position: Int) {
        val userChat = userChats[position]
        holder.bind(userChat)
        holder.itemView.setOnClickListener { onItemClickListener(userChat) }
    }

    override fun getItemCount(): Int {
        return userChats.size
    }

    inner class UserChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.image_avatar)
        private val nameTextView: TextView = itemView.findViewById(R.id.text_name)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.text_last_seen)
        private val onlineStatusView: GifImageView = itemView.findViewById(R.id.online_status)

        fun bind(userChat: UserChat) {
            val chatId = userChat.chatId
            if (chatId != null) {
                val otherUserId = chatId.replace(currentUserId ?: "", "").replace("_", "")

                val userRef = FirebaseDatabase.getInstance().reference.child("users").child(otherUserId)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            nameTextView.text = user.name
                            Glide.with(context).load(user.profileImageUrl).into(avatarImageView)

                            // Lấy trạng thái hoạt động và cập nhật onlineStatusView
                            val presenceRef = FirebaseDatabase.getInstance().reference.child("presence").child(user.uid)
                            presenceRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val status = snapshot.getValue(String::class.java)
                                    val shape = ShapeDrawable(OvalShape())
                                    shape.paint.color = ContextCompat.getColor(
                                        itemView.context,
                                        if (status == "online") R.color.green else R.color.gray
                                    )
                                    onlineStatusView.background = shape
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Xử lý lỗi nếu cần
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Xử lý lỗi nếu cần
                    }
                })
            }

            val lastMessage = userChat.lastMessage
            if (lastMessage != null) {
                lastMessageTextView.text = if (lastMessage.senderId == currentUserId) {
                    if (lastMessage.type == "image") {
                        "Bạn: Đã gửi một ảnh"
                    } else {
                        "Bạn:" + (lastMessage.content?.text ?: "")
                    }
                } else {
                    if (lastMessage.type == "image") {
                        "Đã gửi ảnh cho bạn"
                    } else {
                        lastMessage.content?.text ?: ""
                    }
                }
            } else {
                lastMessageTextView.text = ""
            }
        }
    }
}