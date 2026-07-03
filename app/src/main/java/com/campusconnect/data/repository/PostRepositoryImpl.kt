package com.campusconnect.data.repository

import android.net.Uri
import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.data.remote.dto.UserDto
import com.campusconnect.domain.model.Comment
import com.campusconnect.domain.model.Post
import com.campusconnect.domain.model.PostType
import com.campusconnect.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PostRepository {

    override fun getFeed(lastVisibleTimestamp: Long?, limit: Int): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading)
        try {
            val currentUid = auth.currentUser?.uid
            var query = firestore.collection(Constants.COLLECTION_POSTS)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (lastVisibleTimestamp != null) {
                query = query.startAfter(lastVisibleTimestamp)
            }

            val snapshot = query.get().await()
            val postsList = mutableListOf<Post>()

            for (doc in snapshot.documents) {
                val post = doc.toObject(Post::class.java)
                if (post != null) {
                    // Check if current user liked this post
                    val isLiked = if (currentUid != null) {
                        firestore.collection(Constants.COLLECTION_POSTS)
                            .document(post.postId)
                            .collection(Constants.SUBCOLLECTION_LIKES)
                            .document(currentUid)
                            .get()
                            .await()
                            .exists()
                    } else false

                    postsList.add(post.copy(isLikedByCurrentUser = isLiked))
                }
            }
            emit(Resource.Success(postsList))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch feed"))
        }
    }

    override fun createPost(post: Post, fileUri: Uri?): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            
            // Get current user profile details
            val userDoc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            val userDto = userDoc.toObject(UserDto::class.java) ?: throw Exception("User profile not found")

            val newPostId = firestore.collection(Constants.COLLECTION_POSTS).document().id
            val mediaUrls = mutableListOf<String>()

            // 1. Upload media if present
            if (fileUri != null) {
                val extension = fileUri.toString().substringAfterLast(".", "bin")
                val filename = "${System.currentTimeMillis()}.$extension"
                val storageRef = storage.reference.child("posts/$newPostId/media/$filename")
                storageRef.putFile(fileUri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                mediaUrls.add(downloadUrl)
            }

            // 2. Build complete post
            val finalPost = post.copy(
                postId = newPostId,
                authorId = uid,
                authorName = userDto.name,
                authorUsername = userDto.uniqueUsername,
                authorPhotoUrl = userDto.photoUrl,
                mediaUrls = mediaUrls,
                createdAt = System.currentTimeMillis()
            )

            val batch = firestore.batch()

            // Write unified post doc
            val postRef = firestore.collection(Constants.COLLECTION_POSTS).document(newPostId)
            batch.set(postRef, finalPost)

            // Write type-specific collection doc
            when (post.type) {
                PostType.NOTE -> {
                    val noteRef = firestore.collection(Constants.COLLECTION_NOTES).document(newPostId)
                    batch.set(noteRef, mapOf(
                        "postId" to newPostId,
                        "uploaderId" to uid,
                        "fileUrl" to (mediaUrls.firstOrNull() ?: ""),
                        "createdAt" to finalPost.createdAt
                    ))
                }
                PostType.BLOOD -> {
                    val bloodRef = firestore.collection(Constants.COLLECTION_BLOOD_REQUESTS).document(newPostId)
                    batch.set(bloodRef, mapOf(
                        "postId" to newPostId,
                        "requesterId" to uid,
                        "status" to "open",
                        "createdAt" to finalPost.createdAt
                    ))
                }
                PostType.LOST_FOUND -> {
                    val lfRef = firestore.collection(Constants.COLLECTION_LOST_FOUND).document(newPostId)
                    batch.set(lfRef, mapOf(
                        "postId" to newPostId,
                        "ownerId" to uid,
                        "status" to "lost",
                        "createdAt" to finalPost.createdAt
                    ))
                }
                PostType.RIDE -> {
                    val rideRef = firestore.collection(Constants.COLLECTION_RIDES).document(newPostId)
                    batch.set(rideRef, mapOf(
                        "postId" to newPostId,
                        "driverId" to uid,
                        "status" to "active",
                        "createdAt" to finalPost.createdAt
                    ))
                }
            }

            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create post"))
        }
    }

    override fun toggleLike(postId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val likeRef = firestore.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .collection(Constants.SUBCOLLECTION_LIKES)
                .document(uid)

            val postRef = firestore.collection(Constants.COLLECTION_POSTS).document(postId)

            val likeDoc = likeRef.get().await()
            var isLikedAfterAction = false

            firestore.runTransaction { transaction ->
                if (likeDoc.exists()) {
                    transaction.delete(likeRef)
                    transaction.update(postRef, "likeCount", FieldValue.increment(-1))
                    isLikedAfterAction = false
                } else {
                    transaction.set(likeRef, mapOf(
                        "uid" to uid,
                        "createdAt" to System.currentTimeMillis()
                    ))
                    transaction.update(postRef, "likeCount", FieldValue.increment(1))
                    isLikedAfterAction = true
                }
            }.await()

            emit(Resource.Success(isLikedAfterAction))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to toggle like"))
        }
    }

    override fun getComments(postId: String): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .collection(Constants.SUBCOLLECTION_COMMENTS)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()

            val comments = snapshot.toObjects(Comment::class.java)
            emit(Resource.Success(comments))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch comments"))
        }
    }

    override fun addComment(postId: String, text: String): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading)
        try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val userDoc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
            val userDto = userDoc.toObject(UserDto::class.java) ?: throw Exception("User profile not found")

            val commentsRef = firestore.collection(Constants.COLLECTION_POSTS)
                .document(postId)
                .collection(Constants.SUBCOLLECTION_COMMENTS)
            
            val newCommentId = commentsRef.document().id
            val comment = Comment(
                commentId = newCommentId,
                postId = postId,
                authorId = uid,
                authorName = userDto.name,
                authorUsername = userDto.uniqueUsername,
                authorPhotoUrl = userDto.photoUrl,
                text = text,
                createdAt = System.currentTimeMillis()
            )

            val postRef = firestore.collection(Constants.COLLECTION_POSTS).document(postId)

            firestore.runTransaction { transaction ->
                transaction.set(commentsRef.document(newCommentId), comment)
                transaction.update(postRef, "commentCount", FieldValue.increment(1))
            }.await()

            emit(Resource.Success(comment))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to add comment"))
        }
    }
}
