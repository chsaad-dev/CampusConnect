package com.campusconnect.domain.model

data class NoteDetails(
    val postId: String = "",
    val uploaderId: String = "",
    val subject: String = "",
    val teacher: String = "",
    val fileUrl: String = "",
    val downloads: Int = 0,
    val rating: Float = 0f,
    val department: String = "",
    val createdAt: Long = 0L
)
