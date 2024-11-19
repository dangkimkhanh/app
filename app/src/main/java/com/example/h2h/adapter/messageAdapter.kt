package com.example.h2h.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.glance.visibility
import java.util.Locale
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.h2h.R
import com.example.h2h.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.format

class MessageAdapter(private val context: Context, private val messages: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 0
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                val view = LayoutInflater.from(context).inflate(R.layout.mesage_right, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                val view = LayoutInflater.from(context).inflate(R.layout.mesage_left, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageViewHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text_send)
        private val timeTextView: TextView = itemView.findViewById(R.id.send_time)
        private val messageImageView: ImageView = itemView.findViewById(R.id.message_image_send)
        fun bind(message: Message) {
            if (message.type == "text") {
                messageTextView.text = message.content?.text
                messageTextView.visibility = View.VISIBLE
                messageImageView.visibility = View.GONE
            } else if (message.type == "image") {
                Glide.with(context).load(message.content?.imageUrl).into(messageImageView)
                messageTextView.visibility = View.GONE
                messageImageView.visibility = View.VISIBLE
            }

            // Format timestamp to HH:mm
            val timestamp = message.timestamp ?: 0
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeTextView.text = dateFormat.format(date)
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatar_image)
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text_recived)
        private val timeTextView: TextView = itemView.findViewById(R.id.send_time)
        private val messageImageView: ImageView = itemView.findViewById(R.id.message_image_recived)
        fun bind(message: Message) {
            // Load avatar image using Glide
            Glide.with(context).load(message.senderId).into(avatarImageView)

            if (message.type == "text") {
                messageTextView.text = message.content?.text
                messageTextView.visibility = View.VISIBLE
                messageImageView.visibility = View.GONE
            } else if (message.type == "image") {
                Glide.with(context).load(message.content?.imageUrl).into(messageImageView)
                messageTextView.visibility = View.GONE
                messageImageView.visibility = View.VISIBLE
            }
            // Format timestamp to HH:mm
            val timestamp = message.timestamp ?: 0
            val date = Date(timestamp)
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeTextView.text = dateFormat.format(date)
        }
    }
}