package com.example.h2h.models

data class CreatePost(
    var postId: String? = null,
    val content: String,
    val imageOrVideoUrl: String? = null, // Thay đổi kiểu dữ liệu
    val timestamp: Long,
    val userId: String,
    var likes: Int = 0,
    var comments: Int = 0,
    var shares: Int = 0
)