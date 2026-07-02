package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Comment
import com.example.campusconnect.data.model.Post
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow
import android.net.Uri

interface PostRepository {
    fun getPosts(): Flow<Resource<List<Post>>>
    fun createPost(post: Post, imageUri: Uri?): Flow<Resource<String>>
    fun likePost(postId: String, userId: String): Flow<Resource<String>>
    fun addComment(comment: Comment): Flow<Resource<String>>
    fun getComments(postId: String): Flow<Resource<List<Comment>>>
}
