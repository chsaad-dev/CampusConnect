package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.Comment
import com.campusconnect.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getFeed(lastVisibleTimestamp: Long?, limit: Int): Flow<Resource<List<Post>>>
    fun createPost(post: Post, fileUri: android.net.Uri?): Flow<Resource<Unit>>
    fun toggleLike(postId: String): Flow<Resource<Boolean>>
    fun getComments(postId: String): Flow<Resource<List<Comment>>>
    fun addComment(postId: String, text: String): Flow<Resource<Comment>>
}
