package com.example.campusconnect.di

import android.content.Context
import androidx.room.Room
import com.example.campusconnect.data.local.AppDatabase
import com.example.campusconnect.data.local.BookmarkDao
import com.example.campusconnect.data.local.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "campus_connect_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
}
