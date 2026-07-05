package com.campusconnect.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.campusconnect.data.model.BookmarkEntity
import com.campusconnect.data.model.NoteEntity
import com.campusconnect.data.model.PostEntity
import com.campusconnect.data.model.EventEntity
import com.campusconnect.data.model.JobEntity
import androidx.room.TypeConverters

@Database(
    entities = [
        NoteEntity::class,
        BookmarkEntity::class,
        PostEntity::class,
        EventEntity::class,
        JobEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun jobDao(): JobDao
}
