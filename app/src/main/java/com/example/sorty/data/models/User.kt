package com.example.sorty.data.models

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val birthday: String,
    val email: String,
    val school: String,
    val course: String,
    val imageUri: String?
)