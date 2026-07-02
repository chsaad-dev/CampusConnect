package com.example.campusconnect.data.local

import androidx.room.*
import com.example.campusconnect.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE department = :department AND semester = :semester")
    fun getNotes(department: String, semester: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<NoteEntity>>
}
