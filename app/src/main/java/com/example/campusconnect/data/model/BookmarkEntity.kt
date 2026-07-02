package com.example.campusconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val type: String, // Note, Job, Event
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)
