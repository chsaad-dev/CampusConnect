package com.campusconnect.domain.model

/**
 * Roles a user can have in the system. Controls access to features and admin panel.
 */
enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN;

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.uppercase()) {
                "TEACHER" -> TEACHER
                "ADMIN" -> ADMIN
                else -> STUDENT
            }
        }
    }
}
