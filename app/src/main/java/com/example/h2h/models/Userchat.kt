package com.example.h2h.models

data class UserChat(
    val lastMessage: Message? = null,
    val chatId: String? = null
)

data class LastMessage(
    val senderId: String? = null,
    val content: MessageContent? = null,
    val timestamp: Long? = null,
    val type: String? = null
)

data class MessageContent(
    val text: String? = null,
    val imageUrl: String? = null
)