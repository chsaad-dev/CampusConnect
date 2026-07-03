package com.campusconnect.data.remote.dto

import com.campusconnect.domain.model.User
import com.campusconnect.domain.model.UserRole

/**
 * Firebase-serializable DTO for User documents in Firestore.
 * All fields have defaults for Firestore deserialization.
 */
data class UserDto(
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
    val role: String = "student",
    val themeMode: String = "light",
    val friendsCount: Int = 0,
    val createdAt: Long = 0L,
    val profileComplete: Boolean = false,
    val viewedSubjects: List<String> = emptyList()
) {
    /**
     * Converts DTO to domain model.
     */
    fun toDomain(): User = User(
        uid = uid,
        uniqueUsername = uniqueUsername,
        name = name,
        rollNumber = rollNumber,
        department = department,
        semester = semester,
        phone = phone,
        email = email,
        photoUrl = photoUrl,
        bloodGroup = bloodGroup,
        skills = skills,
        interests = interests,
        bio = bio,
        reputationPoints = reputationPoints,
        role = UserRole.fromString(role),
        themeMode = themeMode,
        friendsCount = friendsCount,
        createdAt = createdAt,
        profileComplete = profileComplete,
        viewedSubjects = viewedSubjects
    )

    companion object {
        /**
         * Converts domain model to DTO for Firestore writes.
         */
        fun fromDomain(user: User): UserDto = UserDto(
            uid = user.uid,
            uniqueUsername = user.uniqueUsername,
            name = user.name,
            rollNumber = user.rollNumber,
            department = user.department,
            semester = user.semester,
            phone = user.phone,
            email = user.email,
            photoUrl = user.photoUrl,
            bloodGroup = user.bloodGroup,
            skills = user.skills,
            interests = user.interests,
            bio = user.bio,
            reputationPoints = user.reputationPoints,
            role = user.role.name.lowercase(),
            themeMode = user.themeMode,
            friendsCount = user.friendsCount,
            createdAt = user.createdAt,
            profileComplete = user.profileComplete,
            viewedSubjects = user.viewedSubjects
        )
    }
}
