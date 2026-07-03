package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEvents(): Flow<Resource<List<Event>>>
    fun getEventDetails(eventId: String): Flow<Resource<Event>>
    fun registerForEvent(eventId: String): Flow<Resource<Unit>>
    fun createEvent(event: Event, bannerUri: android.net.Uri?): Flow<Resource<Unit>>
}
