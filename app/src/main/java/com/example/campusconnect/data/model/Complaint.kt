package com.example.campusconnect.data.model

data class Complaint(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val category: String = "", // WiFi, Lab, Furniture, etc.
    val description: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val status: String = "Pending", // Pending, Accepted, In Progress, Resolved, Rejected
    val priority: String = "Low", // Low, Medium, High
    val timestamp: Long = System.currentTimeMillis(),
    val feedback: String = "",
    val adminId: String = ""
)
