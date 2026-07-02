package com.example.campusconnect.data.local

import androidx.room.*
import com.example.campusconnect.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun removeBookmark(bookmark: BookmarkEntity)

    @Query("SELECT EXISTS(SELECT * FROM bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean
}
