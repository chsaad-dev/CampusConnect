package com.example.campusconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val fileUrl: String,
    val department: String,
    val semester: String,
    val uploaderName: String,
    val isDownloaded: Boolean = false
)

fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    subject = subject,
    fileUrl = fileUrl,
    department = department,
    semester = semester,
    uploaderName = uploaderName,
    isDownloaded = true
)

fun NoteEntity.toNote() = Note(
    id = id,
    title = title,
    subject = subject,
    fileUrl = fileUrl,
    department = department,
    semester = semester,
    uploaderName = uploaderName
)
