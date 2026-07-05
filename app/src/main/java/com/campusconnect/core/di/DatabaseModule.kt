package com.campusconnect.core.di

import android.content.Context
import androidx.room.Room
import com.campusconnect.data.local.AppDatabase
import com.campusconnect.data.local.BookmarkDao
import com.campusconnect.data.local.NoteDao
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

    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): com.campusconnect.data.local.PostDao {
        return database.postDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase): com.campusconnect.data.local.EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideJobDao(database: AppDatabase): com.campusconnect.data.local.JobDao {
        return database.jobDao()
    }
}
