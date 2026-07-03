package com.campusconnect.domain.model

data class Post(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorUsername: String = "",
    val authorPhotoUrl: String = "",
    val type: PostType = PostType.NOTE,
    val caption: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: MediaType = MediaType.NONE,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long = 0L,
    val refId: String = "",
    val department: String = "",
    val visibility: String = "public",
    val isLikedByCurrentUser: Boolean = false
)

enum class PostType {
    NOTE,
    BLOOD,
    LOST_FOUND,
    RIDE;

    companion object {
        fun fromString(value: String): PostType {
            return when (value.uppercase()) {
                "BLOOD" -> BLOOD
                "LOST_FOUND" -> LOST_FOUND
                "RIDE" -> RIDE
                else -> NOTE
            }
        }
    }
}

enum class MediaType {
    IMAGE,
    VIDEO,
    PDF,
    DOCX,
    PPT,
    NONE;

    companion object {
        fun fromString(value: String): MediaType {
            return when (value.uppercase()) {
                "IMAGE" -> IMAGE
                "VIDEO" -> VIDEO
                "PDF" -> PDF
                "DOCX" -> DOCX
                "PPT" -> PPT
                else -> NONE
            }
        }
    }
}
