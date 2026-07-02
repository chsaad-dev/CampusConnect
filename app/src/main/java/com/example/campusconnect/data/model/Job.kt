package com.example.campusconnect.data.model

data class Job(
    val id: String = "",
    val title: String = "",
    val companyName: String = "",
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val location: String = "",
    val type: String = "Internship", // Internship, Part-time, Full-time
    val category: String = "", // Software, Business, etc.
    val applyLink: String = "",
    val salaryRange: String = "",
    val postedTimestamp: Long = System.currentTimeMillis(),
    val deadline: String = ""
)
