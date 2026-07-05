package com.campusconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.campusconnect.domain.model.Event

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val hostType: String = "Admin",
    val date: Long = 0L,
    val location: String = "",
    val bannerUrl: String = "",
    val registeredUsers: List<String> = emptyList(),
    val qrCode: String = "",
    val createdAt: Long = 0L,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Event = Event(
        eventId = eventId,
        title = title,
        description = description,
        hostType = hostType,
        date = date,
        location = location,
        bannerUrl = bannerUrl,
        registeredUsers = registeredUsers,
        qrCode = qrCode,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(event: Event): EventEntity = EventEntity(
            eventId = event.eventId,
            title = event.title,
            description = event.description,
            hostType = event.hostType,
            date = event.date,
            location = event.location,
            bannerUrl = event.bannerUrl,
            registeredUsers = event.registeredUsers,
            qrCode = event.qrCode,
            createdAt = event.createdAt
        )
    }
}
