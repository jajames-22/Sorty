package com.example.sorty.data.models

// We use 'SubjectFile' to avoid conflict with the system 'java.io.File'
data class SubjectFile(
    val id: Long,
    val name: String,
    val uri: String,
    val type: String,
    val subjectName: String
)