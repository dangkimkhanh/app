package com.example.h2h.models

data class CreatePost (
    val postId: String,
    val content: String,
    val images: List<String>? = null,
    val timestamp: Long,
    val userId: String,
    val userName: String? = null,
    val userAvatar: String? = null,
    var likes: Int = 0,
    var comments: Int = 0,
    var shares: Int = 0
)
