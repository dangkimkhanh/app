package com.example.h2h.models

data class Relationship(
    val userId1: String = "",
    val userId2: String = "",
    val relationshipType: String = "" // "friend" hoặc "follower"
)