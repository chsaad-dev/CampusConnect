package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Event
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEvents(): Flow<Resource<List<Event>>>
    fun createEvent(event: Event): Flow<Resource<String>>
    fun registerForEvent(eventId: String, userId: String): Flow<Resource<String>>
    fun verifyAttendance(eventId: String, userId: String): Flow<Resource<String>>
    fun getUpcomingEvents(): Flow<Resource<List<Event>>>
}
