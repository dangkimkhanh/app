package com.example.h2h.models

data class Comment(
    val commentId: String? = null,
    val content: String,
    val userId: String,
    val userName: String? = null,
    val userAvatar: String? = null,
    val timestamp: Any? = null // Sử dụng Any? để có thể lưu trữ ServerValue.TIMESTAMP
)
