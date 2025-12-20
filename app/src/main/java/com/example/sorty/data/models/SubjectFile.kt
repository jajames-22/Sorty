package com.example.sorty.data.models

/**
 * Model representing a file attached to a specific subject.
 */
data class SubjectFile(
    val id: Long,
    val name: String,
    val uri: String,
    val type: String,
    val subjectName: String
)