package com.example.campusconnect.data.model

data class Attendance(
    val id: String = "",
    val eventId: String = "",
    val studentId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Verified"
)
