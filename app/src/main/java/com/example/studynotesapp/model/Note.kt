package com.example.studynotesapp.model

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val hasReminder: Boolean = false // ✅ New field added
)
