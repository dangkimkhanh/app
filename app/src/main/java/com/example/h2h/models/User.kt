package com.example.h2h.models

import com.google.firebase.Firebase
import com.google.firebase.auth.auth

data class User(
    val uid: String = Firebase.auth.currentUser?.uid ?: "",
    var name: String = "",
    var dateOfBirth: String = "",
    var gender: String = "",
    var profileImageUrl: String? = null, // URL ảnh đại diện
    var coverImageUrl: String? = null // URL ảnh bìa
) {

}