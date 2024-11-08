package com.example.h2h.models

import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val senderAvatarUrl: String = "",
    val isSentByCurrentUser: Boolean = false
) {
    fun getLastMessage(): String {
        return if (isSentByCurrentUser) {
            "Bạn: $content"
        } else {
            content
        }
    }

    val formattedTimestamp: String
        get() {
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return dateFormat.format(Date(timestamp))
        }
}