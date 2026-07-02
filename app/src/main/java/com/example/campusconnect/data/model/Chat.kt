package com.example.campusconnect.data.model

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val type: String = "Private" // Private, Group, Society, Complaint, Ride, Blood
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val seen: Boolean = false
)
