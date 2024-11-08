package com.example.h2h.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.compose.material.placeholder
import com.bumptech.glide.Glide
import com.example.h2h.R
import com.example.h2h.models.Message
import de.hdodenhof.circleimageview.CircleImageView

class ItemChatAdapter(private val chatList: List<Message>) : RecyclerView.Adapter<ItemChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: CircleImageView = itemView.findViewById(R.id.image_avatar)
        private val nameTextView: TextView = itemView.findViewById(R.id.text_name)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.text_last_seen)
        //private val timestampTextView: TextView = itemView.findViewById(R.id.text_timestamp) // Thêm TextView cho thời gian
        private val onlineStatusView: View = itemView.findViewById(R.id.online_status)

        fun bind(chat: Message) {
            Glide.with(itemView.context)
                .load(chat.senderAvatarUrl)
                .placeholder(R.drawable.ic_avatar)
                .into(avatarImageView)

            nameTextView.text = chat.senderName
            lastMessageTextView.text = chat.getLastMessage()
            //timestampTextView.text = chat.formattedTimestamp
            // Hiển thị online status nếu cần
            onlineStatusView.visibility = if (chat.isSentByCurrentUser) View.VISIBLE else View.GONE
        }
    }
}