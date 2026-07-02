package com.example.campusconnect.data.model

data class Note(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileType: String = "", // PDF, PPT, DOCX, Image
    val uploaderId: String = "",
    val uploaderName: String = "",
    val department: String = "",
    val semester: String = "",
    val subject: String = "",
    val teacherName: String = "",
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val downloadsCount: Int = 0,
    val fileSize: String = "0 KB",
    val rating: Float = 0f,
    val ratingsCount: Int = 0
)
