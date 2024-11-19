package com.example.h2h.models

data class Message(
    val senderId: String? = null,
    val receiverId: String? = null,
    val type: String? = null,
    val content: MessageContent? = null,
    val timestamp: Long? = null
)
