package com.campusconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.campusconnect.data.model.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC")
    suspend fun getCachedEvents(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("DELETE FROM events")
    suspend fun clearAllEvents()
}
