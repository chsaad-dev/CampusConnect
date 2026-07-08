package com.campusconnect.domain.model

data class TeamMember(
    val name: String,
    val role: String,
    val avatarRes: Int,
    val githubUrl: String? = null,
    val linkedinUrl: String? = null
)
