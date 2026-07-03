package com.campusconnect.domain.model

/**
 * Domain model for a User. Maps to Firestore `users/{uid}` document.
 */
data class User(
    val uid: String = "",
    val uniqueUsername: String = "",
    val name: String = "",
    val rollNumber: String = "",
    val department: String = "",
    val semester: Int = 0,
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val bloodGroup: String = "",
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val bio: String = "",
    val reputationPoints: Int = 0,
    val role: UserRole = UserRole.STUDENT,
    val themeMode: String = "light",
    val friendsCount: Int = 0,
    val createdAt: Long = 0L,
    val profileComplete: Boolean = false,
    val viewedSubjects: List<String> = emptyList()
)
