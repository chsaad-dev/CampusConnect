package com.example.campusconnect.data.repository

import android.net.Uri
import com.example.campusconnect.data.model.Comment
import com.example.campusconnect.data.model.Post
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PostRepository {

    override fun getPosts(): Flow<Resource<List<Post>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching posts"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.toObjects(Post::class.java)
                    trySend(Resource.Success(posts))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun createPost(post: Post, imageUri: Uri?): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            var imageUrl = ""
            if (imageUri != null) {
                val fileName = "posts/${System.currentTimeMillis()}_${post.authorId}"
                val storageRef = storage.reference.child(fileName)
                storageRef.putFile(imageUri).await()
                imageUrl = storageRef.downloadUrl.await().toString()
            }

            val postId = firestore.collection("posts").document().id
            val newPost = post.copy(id = postId, imageUrl = imageUrl)
            firestore.collection("posts").document(postId).set(newPost).await()
            trySend(Resource.Success("Post created"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to create post"))
        }
        awaitClose()
    }

    override fun likePost(postId: String, userId: String): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("posts").document(postId)
                .update("likesCount", FieldValue.increment(1)).await()
            trySend(Resource.Success("Liked"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to like"))
        }
        awaitClose()
    }

    override fun addComment(comment: Comment): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val commentId = firestore.collection("posts").document(comment.postId)
                .collection("comments").document().id
            val newComment = comment.copy(id = commentId)
            
            firestore.collection("posts").document(comment.postId)
                .collection("comments").document(commentId).set(newComment).await()
            
            firestore.collection("posts").document(comment.postId)
                .update("commentsCount", FieldValue.increment(1)).await()
            
            trySend(Resource.Success("Comment added"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to add comment"))
        }
        awaitClose()
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching comments"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.toObjects(Comment::class.java)
                    trySend(Resource.Success(comments))
                }
            }
        awaitClose { subscription.remove() }
    }
}
