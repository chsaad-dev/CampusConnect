package com.campusconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val postId: String = "",
    val type: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
