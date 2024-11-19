package com.example.h2h.models

data class Comment(
    val id: String? = null,
    val postId: String? = null,
    val content: String? = null,
    val userId: String? = null,
    val timestamp: Long? = null,
    val parentId: String? = null,
    val replies: MutableList<Comment> = mutableListOf() // Danh sách các comment reply
)