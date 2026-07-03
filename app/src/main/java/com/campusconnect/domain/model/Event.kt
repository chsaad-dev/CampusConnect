package com.campusconnect.domain.model

data class Event(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val hostType: String = "Admin", // Admin, Society
    val date: Long = 0L,
    val location: String = "",
    val bannerUrl: String = "",
    val registeredUsers: List<String> = emptyList(),
    val qrCode: String = "",
    val createdAt: Long = 0L
)
