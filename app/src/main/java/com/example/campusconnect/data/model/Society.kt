package com.example.campusconnect.data.model

data class Society(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "", // Technical, Cultural, Sports
    val logoUrl: String = "",
    val adminId: String = "",
    val membersCount: Int = 0,
    val upcomingEventsCount: Int = 0
)
