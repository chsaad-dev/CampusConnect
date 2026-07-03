package com.campusconnect.domain.model

data class RideDetails(
    val postId: String = "",
    val driverId: String = "",
    val from: String = "",
    val to: String = "",
    val seatsTotal: Int = 0,
    val seatsLeft: Int = 0,
    val cost: String = "",
    val status: String = "active", // active, full, completed
    val createdAt: Long = 0L
)
