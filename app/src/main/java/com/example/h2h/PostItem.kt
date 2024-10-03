package com.example.h2h
data class Post(
    val userName: String,
    val postTime: String,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int
)