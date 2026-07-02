package com.example.campusconnect.data.model

data class Ride(
    val id: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val departureTime: String = "",
    val availableSeats: Int = 0,
    val costPerSeat: String = "",
    val carModel: String = "",
    val carNumber: String = "",
    val status: String = "Available", // Available, Full, Completed, Cancelled
    val timestamp: Long = System.currentTimeMillis(),
    val passengers: List<String> = emptyList() // List of User IDs
)
