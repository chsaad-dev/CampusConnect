package com.example.campusconnect.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val rollNumber: String = "",
    val email: String = "",
    val department: String = "",
    val semester: String = "1",
    val phoneNumber: String = "",
    val profilePicture: String = "",
    val bloodGroup: String = "",
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val badges: List<String> = emptyList(),
    val reputationPoints: Int = 0,
    val role: String = "Student", // Student, Teacher, Admin
    val isVerified: Boolean = false,
    val fcmToken: String = ""
)
