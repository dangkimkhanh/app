package com.example.h2h.models

data class CreatePost(
    var content: String? = null,
    var imageOrVideoUrl: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var postId: String? = null,
    var likes: Map<String, Boolean> = emptyMap() , // Danh sách rỗng khi khởi tạo
    var comments:Map<String, Comment> = emptyMap() , // Danh sách rỗng khi khởi tạo
    var shares: Map<String, Boolean> = emptyMap()  // Danh sách rỗng khi khởi tạo
)