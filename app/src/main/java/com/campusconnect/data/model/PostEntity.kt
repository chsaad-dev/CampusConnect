package com.campusconnect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.campusconnect.domain.model.MediaType
import com.campusconnect.domain.model.Post
import com.campusconnect.domain.model.PostType

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
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
    val isLikedByCurrentUser: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Post = Post(
        postId = postId,
        authorId = authorId,
        authorName = authorName,
        authorUsername = authorUsername,
        authorPhotoUrl = authorPhotoUrl,
        type = type,
        caption = caption,
        mediaUrls = mediaUrls,
        mediaType = mediaType,
        likeCount = likeCount,
        commentCount = commentCount,
        createdAt = createdAt,
        refId = refId,
        department = department,
        visibility = visibility,
        isLikedByCurrentUser = isLikedByCurrentUser
    )

    companion object {
        fun fromDomain(post: Post): PostEntity = PostEntity(
            postId = post.postId,
            authorId = post.authorId,
            authorName = post.authorName,
            authorUsername = post.authorUsername,
            authorPhotoUrl = post.authorPhotoUrl,
            type = post.type,
            caption = post.caption,
            mediaUrls = post.mediaUrls,
            mediaType = post.mediaType,
            likeCount = post.likeCount,
            commentCount = post.commentCount,
            createdAt = post.createdAt,
            refId = post.refId,
            department = post.department,
            visibility = post.visibility,
            isLikedByCurrentUser = post.isLikedByCurrentUser
        )
    }
}
