package com.campusconnect.data.repository

import android.net.Uri
import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.data.remote.dto.UserDto
import com.campusconnect.domain.model.BloodRequestDetails
import com.campusconnect.domain.model.Comment
import com.campusconnect.domain.model.LostFoundDetails
import com.campusconnect.domain.model.NoteDetails
import com.campusconnect.domain.model.Post
import com.campusconnect.domain.model.PostType
import com.campusconnect.domain.model.RideDetails
import com.campusconnect.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.campusconnect.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val mediaRepository: MediaRepository,
    private val postDao: com.campusconnect.data.local.PostDao
) : PostRepository {

    override fun getFeed(lastVisibleTimestamp: Long?, limit: Int): Flow<Resource<List<Post>>> = flow {
        if (lastVisibleTimestamp == null) {
            val cachedEntities = postDao.getCachedPosts()
            if (cachedEntities.isNotEmpty()) {
                emit(Resource.Success(cachedEntities.map { it.toDomain() }))
            } else {
                emit(Resource.Loading)
            }
        } else {
            emit(Resource.Loading)
        }
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

            if (lastVisibleTimestamp == null) {
                postDao.clearAllPosts()
                postDao.insertPosts(postsList.map { com.campusconnect.data.model.PostEntity.fromDomain(it) })
            }

            emit(Resource.Success(postsList))
        } catch (e: Exception) {
            val cachedEntities = postDao.getCachedPosts()
            if (lastVisibleTimestamp != null || cachedEntities.isEmpty()) {
                emit(Resource.Error(e.message ?: "Failed to fetch feed"))
            }
        }
    }

    override fun createPost(post: Post, fileUri: Uri?, extraData: Map<String, Any>): Flow<Resource<Unit>> = flow {
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
                val downloadUrl = when (post.mediaType) {
                    com.campusconnect.domain.model.MediaType.IMAGE -> mediaRepository.uploadImage(fileUri)
                    com.campusconnect.domain.model.MediaType.VIDEO -> mediaRepository.uploadVideo(fileUri)
                    else -> mediaRepository.uploadDocument(fileUri)
                }
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
                    val noteData = extraData.toMutableMap()
                    noteData["postId"] = newPostId
                    noteData["uploaderId"] = uid
                    noteData["fileUrl"] = mediaUrls.firstOrNull() ?: ""
                    noteData["department"] = finalPost.department
                    noteData["createdAt"] = finalPost.createdAt
                    batch.set(noteRef, noteData)
                }
                PostType.BLOOD -> {
                    val bloodRef = firestore.collection(Constants.COLLECTION_BLOOD_REQUESTS).document(newPostId)
                    val bloodData = extraData.toMutableMap()
                    bloodData["postId"] = newPostId
                    bloodData["requesterId"] = uid
                    bloodData["status"] = "open"
                    bloodData["createdAt"] = finalPost.createdAt
                    batch.set(bloodRef, bloodData)
                }
                PostType.LOST_FOUND -> {
                    val lfRef = firestore.collection(Constants.COLLECTION_LOST_FOUND).document(newPostId)
                    val lfData = extraData.toMutableMap()
                    lfData["postId"] = newPostId
                    lfData["ownerId"] = uid
                    lfData["status"] = "lost"
                    lfData["createdAt"] = finalPost.createdAt
                    batch.set(lfRef, lfData)
                }
                PostType.RIDE -> {
                    val rideRef = firestore.collection(Constants.COLLECTION_RIDES).document(newPostId)
                    val rideData = extraData.toMutableMap()
                    rideData["postId"] = newPostId
                    rideData["driverId"] = uid
                    rideData["status"] = "active"
                    rideData["createdAt"] = finalPost.createdAt
                    batch.set(rideRef, rideData)
                }
            }

            batch.commit().await()

            // Trigger notification logs to matching target users
            triggerPostNotifications(finalPost, extraData)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to create post"))
        }
    }

    private suspend fun triggerPostNotifications(post: Post, extraData: Map<String, Any>) {
        val uid = auth.currentUser?.uid ?: return
        val batch = firestore.batch()
        var hasNotifs = false

        try {
            when (post.type) {
                PostType.NOTE -> {
                    val dept = extraData["department"] as? String ?: ""
                    if (dept.isNotEmpty()) {
                        val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                            .whereEqualTo("department", dept)
                            .get()
                            .await()

                        for (doc in snapshot.documents) {
                            val targetUid = doc.id
                            if (targetUid != uid) {
                                val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                                    .document(targetUid)
                                    .collection("items")
                                    .document()

                                val notif = com.campusconnect.domain.model.NotificationItem(
                                    notifId = notifRef.id,
                                    title = "New Notes Shared",
                                    body = "New study notes for '${extraData["subject"] ?: "your course"}' are available in your department.",
                                    type = "note",
                                    refId = post.postId,
                                    createdAt = System.currentTimeMillis()
                                )
                                batch.set(notifRef, notif)
                                hasNotifs = true
                            }
                        }
                    }
                }
                PostType.BLOOD -> {
                    val bloodGroup = extraData["bloodGroup"] as? String ?: ""
                    if (bloodGroup.isNotEmpty()) {
                        val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                            .whereEqualTo("bloodGroup", bloodGroup)
                            .get()
                            .await()

                        for (doc in snapshot.documents) {
                            val targetUid = doc.id
                            if (targetUid != uid) {
                                val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                                    .document(targetUid)
                                    .collection("items")
                                    .document()

                                val notif = com.campusconnect.domain.model.NotificationItem(
                                    notifId = notifRef.id,
                                    title = "URGENT: Blood Request",
                                    body = "A blood request for group '$bloodGroup' has been posted at ${extraData["hospital"] ?: "nearby hospital"}.",
                                    type = "blood_request",
                                    refId = post.postId,
                                    createdAt = System.currentTimeMillis()
                                )
                                batch.set(notifRef, notif)
                                hasNotifs = true
                            }
                        }
                    }
                }
                PostType.RIDE -> {
                    // Notify users in driver's department
                    val driverDoc = firestore.collection(Constants.COLLECTION_USERS).document(uid).get().await()
                    val dept = driverDoc.getString("department") ?: ""
                    if (dept.isNotEmpty()) {
                        val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                            .whereEqualTo("department", dept)
                            .get()
                            .await()

                        for (doc in snapshot.documents) {
                            val targetUid = doc.id
                            if (targetUid != uid) {
                                val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                                    .document(targetUid)
                                    .collection("items")
                                    .document()

                                val notif = com.campusconnect.domain.model.NotificationItem(
                                    notifId = notifRef.id,
                                    title = "New Ride Shared",
                                    body = "${post.authorName} offered a ride: ${extraData["from"]} -> ${extraData["to"]}.",
                                    type = "ride",
                                    refId = post.postId,
                                    createdAt = System.currentTimeMillis()
                                )
                                batch.set(notifRef, notif)
                                hasNotifs = true
                            }
                        }
                    }
                }
                else -> {}
            }

            if (hasNotifs) {
                batch.commit().await()
            }
        } catch (e: Exception) {
            // Log or ignore notification failure to prevent blocking post creation
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

    override fun getNoteDetails(postId: String): Flow<Resource<NoteDetails>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_NOTES).document(postId).get().await()
            val details = doc.toObject(NoteDetails::class.java) ?: throw Exception("Details not found")
            emit(Resource.Success(details))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch note details"))
        }
    }

    override fun getBloodRequestDetails(postId: String): Flow<Resource<BloodRequestDetails>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_BLOOD_REQUESTS).document(postId).get().await()
            val details = doc.toObject(BloodRequestDetails::class.java) ?: throw Exception("Details not found")
            emit(Resource.Success(details))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch blood request details"))
        }
    }

    override fun getLostFoundDetails(postId: String): Flow<Resource<LostFoundDetails>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_LOST_FOUND).document(postId).get().await()
            val details = doc.toObject(LostFoundDetails::class.java) ?: throw Exception("Details not found")
            emit(Resource.Success(details))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch lost & found details"))
        }
    }

    override fun getRideDetails(postId: String): Flow<Resource<RideDetails>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_RIDES).document(postId).get().await()
            val details = doc.toObject(RideDetails::class.java) ?: throw Exception("Details not found")
            emit(Resource.Success(details))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch ride details"))
        }
    }

    override fun getAllNotes(): Flow<Resource<List<NoteDetails>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_NOTES)
                .get()
                .await()
            val list = snapshot.toObjects(NoteDetails::class.java)
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch notes"))
        }
    }

    override fun getRecentStoryAuthors(): Flow<Resource<List<com.campusconnect.domain.model.User>>> = flow {
        emit(Resource.Loading)
        try {
            val oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            val snapshot = firestore.collection(Constants.COLLECTION_POSTS)
                .whereGreaterThanOrEqualTo("createdAt", oneDayAgo)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val postsList = snapshot.toObjects(Post::class.java)
            val distinctAuthors = postsList.distinctBy { it.authorId }.map { post ->
                com.campusconnect.domain.model.User(
                    uid = post.authorId,
                    name = post.authorName,
                    photoUrl = post.authorPhotoUrl
                )
            }
            emit(Resource.Success(distinctAuthors))
        } catch (e: java.lang.Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch recent story authors"))
        }
    }

    override fun searchPosts(query: String): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading)
        try {
            val currentUid = auth.currentUser?.uid
            val snapshot = firestore.collection(Constants.COLLECTION_POSTS)
                .orderBy(Constants.FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val postsList = mutableListOf<Post>()
            for (doc in snapshot.documents) {
                val post = doc.toObject(Post::class.java)
                if (post != null) {
                    val matches = post.caption.contains(query, ignoreCase = true) ||
                            post.authorName.contains(query, ignoreCase = true) ||
                            post.authorUsername.contains(query, ignoreCase = true)
                    
                    if (matches) {
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
            }
            emit(Resource.Success(postsList))
        } catch (e: java.lang.Exception) {
            emit(Resource.Error(e.message ?: "Failed to search posts"))
        }
    }
}
