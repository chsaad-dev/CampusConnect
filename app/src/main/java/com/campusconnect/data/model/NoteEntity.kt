package com.campusconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val noteId: String = "",
    val postId: String = "",
    val uploaderId: String = "",
    val title: String = "",
    val subject: String = "",
    val department: String = "",
    val semester: Int = 0,
    val teacher: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val downloads: Int = 0,
    val avgRating: Float = 0f,
    val cachedAt: Long = System.currentTimeMillis()
)
