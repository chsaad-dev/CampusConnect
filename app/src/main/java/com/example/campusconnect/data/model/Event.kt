package com.example.campusconnect.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "", // Seminar, Workshop, Sports, etc.
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val organizer: String = "", // Society name or University
    val imageUrl: String = "",
    val registrationLink: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val attendeesCount: Int = 0
)
