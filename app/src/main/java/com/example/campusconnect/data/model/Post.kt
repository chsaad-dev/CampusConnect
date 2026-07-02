package com.example.campusconnect.data.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "Student", // Student, Teacher, Society
    val authorProfilePictureUrl: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val type: String = "Announcement" // Announcement, Achievement, EventUpdate
)

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
