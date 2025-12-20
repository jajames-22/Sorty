package com.example.sorty.data.models

data class Subject(
    val id: Int,
    val name: String,
    val description: String,
    val color: String,
    val isArchived: Boolean,
    val userEmail: String,
    val ownerEmail: String
)