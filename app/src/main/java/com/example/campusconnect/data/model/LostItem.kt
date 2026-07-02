package com.example.campusconnect.data.model

data class LostItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val reward: String = "",
    val uploaderId: String = "",
    val uploaderName: String = "",
    val contactNumber: String = "",
    val status: String = "Lost", // Lost, Found, Claimed
    val timestamp: Long = System.currentTimeMillis()
)
