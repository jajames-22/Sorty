package com.example.sorty.data.models

/**
 * Represents a user who has been granted access to a shared folder.
 * This is a shared model used by DatabaseHelper, CourseActivity, and ShareBottomSheet.
 */
data class SharedUser(
    val email: String,
    val firstName: String,
    val lastName: String,
    val imageUri: String?
)