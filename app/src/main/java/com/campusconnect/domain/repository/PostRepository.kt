package com.campusconnect.domain.repository

import com.campusconnect.core.common.Resource
import com.campusconnect.domain.model.BloodRequestDetails
import com.campusconnect.domain.model.Comment
import com.campusconnect.domain.model.LostFoundDetails
import com.campusconnect.domain.model.NoteDetails
import com.campusconnect.domain.model.Post
import com.campusconnect.domain.model.RideDetails
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getFeed(lastVisibleTimestamp: Long?, limit: Int): Flow<Resource<List<Post>>>
    fun createPost(post: Post, fileUri: android.net.Uri?, extraData: Map<String, Any> = emptyMap()): Flow<Resource<Unit>>
    fun toggleLike(postId: String): Flow<Resource<Boolean>>
    fun getComments(postId: String): Flow<Resource<List<Comment>>>
    fun addComment(postId: String, text: String): Flow<Resource<Comment>>
    
    fun getNoteDetails(postId: String): Flow<Resource<NoteDetails>>
    fun getBloodRequestDetails(postId: String): Flow<Resource<BloodRequestDetails>>
    fun getLostFoundDetails(postId: String): Flow<Resource<LostFoundDetails>>
    fun getRideDetails(postId: String): Flow<Resource<RideDetails>>
    fun getAllNotes(): Flow<Resource<List<NoteDetails>>>
    fun getRecentStoryAuthors(): Flow<Resource<List<com.campusconnect.domain.model.User>>>
    fun searchPosts(query: String): Flow<Resource<List<Post>>>
    fun getStatusByUserId(userId: String): Flow<Resource<List<Post>>>
    fun getPostById(postId: String): Flow<Resource<Post>>
}
