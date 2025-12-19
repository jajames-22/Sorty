package com.example.sorty.ui.subjects

data class Subject(
    val id: Int,
    val name: String,
    val description: String,
    val color: String,
    val isArchived: Boolean = false
)