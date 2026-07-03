package com.campusconnect.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.campusconnect.data.model.BookmarkEntity
import com.campusconnect.data.model.NoteEntity

@Database(entities = [NoteEntity::class, BookmarkEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun bookmarkDao(): BookmarkDao
}
