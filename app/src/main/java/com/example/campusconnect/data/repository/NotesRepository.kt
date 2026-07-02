package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Note
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow
import android.net.Uri

interface NotesRepository {
    fun uploadNote(note: Note, fileUri: Uri): Flow<Resource<String>>
    fun getNotes(department: String, semester: String): Flow<Resource<List<Note>>>
    fun searchNotes(query: String): Flow<Resource<List<Note>>>
    fun addRating(noteId: String, rating: Float): Flow<Resource<String>>
    fun incrementDownloadCount(noteId: String): Flow<Resource<String>>
    fun getRecommendedNotes(department: String, interests: List<String>): Flow<Resource<List<Note>>>
    fun getPopularNotes(): Flow<Resource<List<Note>>>
    fun downloadNote(note: Note)
}
