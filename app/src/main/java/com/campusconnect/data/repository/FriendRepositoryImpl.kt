package com.campusconnect.data.repository

import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.data.remote.dto.UserDto
import com.campusconnect.domain.model.FriendRequest
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.FriendRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FriendRepository {

    private val currentUid: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    override fun searchUsers(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            val q = query.trim()
            if (q.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }
            
            // Search by username prefix
            val snapshot = firestore.collection(Constants.COLLECTION_POSTS) // wait, users are in users collection
            val usersSnapshot = firestore.collection(Constants.COLLECTION_USERS)
                .orderBy("uniqueUsername")
                .startAt(q)
                .endAt(q + "\uf8ff")
                .limit(20)
                .get()
                .await()

            val users = usersSnapshot.documents.mapNotNull { doc ->
                val dto = doc.toObject(UserDto::class.java)
                dto?.let {
                    User(
                        uid = it.uid,
                        uniqueUsername = it.uniqueUsername,
                        name = it.name,
                        rollNumber = it.rollNumber,
                        department = it.department,
                        semester = it.semester,
                        photoUrl = it.photoUrl,
                        bloodGroup = it.bloodGroup,
                        friendsCount = it.friendsCount
                    )
                }
            }.filter { it.uid != currentUid }

            emit(Resource.Success(users))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Search failed"))
        }
    }

    override fun sendFriendRequest(toUser: User): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val fromUid = currentUid
            
            // Get sender profile details
            val senderDoc = firestore.collection(Constants.COLLECTION_USERS).document(fromUid).get().await()
            val senderDto = senderDoc.toObject(UserDto::class.java) ?: throw Exception("Sender profile not found")

            val requestId = "${fromUid}_${toUser.uid}"
            val request = FriendRequest(
                requestId = requestId,
                fromUid = fromUid,
                fromUsername = senderDto.uniqueUsername,
                fromName = senderDto.name,
                fromPhotoUrl = senderDto.photoUrl,
                toUid = toUser.uid,
                toUsername = toUser.uniqueUsername,
                toName = toUser.name,
                toPhotoUrl = toUser.photoUrl,
                status = "pending",
                createdAt = System.currentTimeMillis()
            )

            val batch = firestore.batch()
            batch.set(
                firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS).document(requestId),
                request
            )

            // Write notification log to recipient
            val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(toUser.uid)
                .collection("items")
                .document()

            val notifItem = com.campusconnect.domain.model.NotificationItem(
                notifId = notifRef.id,
                title = "New Friend Request",
                body = "${senderDto.name} (@${senderDto.uniqueUsername}) sent you a friend request.",
                type = "friend_request",
                refId = fromUid,
                createdAt = request.createdAt
            )
            batch.set(notifRef, notifItem)

            batch.commit().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send request"))
        }
    }

    override fun getPendingRequests(): Flow<Resource<List<FriendRequest>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo("toUid", currentUid)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val requests = snapshot.toObjects(FriendRequest::class.java)
            emit(Resource.Success(requests))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch requests"))
        }
    }

    override fun getFriends(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection(Constants.COLLECTION_USERS)
                .document(currentUid)
                .collection("friends")
                .get()
                .await()

            val friendUids = snapshot.documents.map { it.id }
            if (friendUids.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Fetch profiles of all friends
            val friends = mutableListOf<User>()
            // Firestore whereIn supports up to 10/30 elements, let's chunk in 10s
            friendUids.chunked(10).forEach { chunk ->
                val profiles = firestore.collection(Constants.COLLECTION_USERS)
                    .whereIn("uid", chunk)
                    .get()
                    .await()
                
                profiles.documents.forEach { doc ->
                    val dto = doc.toObject(UserDto::class.java)
                    dto?.let {
                        friends.add(User(
                            uid = it.uid,
                            uniqueUsername = it.uniqueUsername,
                            name = it.name,
                            photoUrl = it.photoUrl,
                            department = it.department,
                            friendsCount = it.friendsCount
                        ))
                    }
                }
            }

            emit(Resource.Success(friends))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch friends"))
        }
    }

    override fun acceptFriendRequest(request: FriendRequest): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val batch = firestore.batch()

            // 1. Update friend request status
            val requestRef = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS).document(request.requestId)
            batch.update(requestRef, "status", "accepted")

            // 2. Add to both users' friends list subcollection
            val userAFriendsRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(request.fromUid)
                .collection("friends")
                .document(request.toUid)
            
            val userBFriendsRef = firestore.collection(Constants.COLLECTION_USERS)
                .document(request.toUid)
                .collection("friends")
                .document(request.fromUid)

            batch.set(userAFriendsRef, mapOf("addedAt" to System.currentTimeMillis()))
            batch.set(userBFriendsRef, mapOf("addedAt" to System.currentTimeMillis()))

            // 3. Increment friendsCount for both users
            val userARef = firestore.collection(Constants.COLLECTION_USERS).document(request.fromUid)
            val userBRef = firestore.collection(Constants.COLLECTION_USERS).document(request.toUid)

            batch.update(userARef, "friendsCount", FieldValue.increment(1))
            batch.update(userBRef, "friendsCount", FieldValue.increment(1))

            // 4. Write notification to request sender
            val notifRef = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(request.fromUid)
                .collection("items")
                .document()

            val notifItem = com.campusconnect.domain.model.NotificationItem(
                notifId = notifRef.id,
                title = "Friend Request Accepted",
                body = "${request.toName} (@${request.toUsername}) accepted your friend request.",
                type = "friend_request",
                refId = request.toUid,
                createdAt = System.currentTimeMillis()
            )
            batch.set(notifRef, notifItem)

            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to accept request"))
        }
    }

    override fun rejectFriendRequest(request: FriendRequest): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                .document(request.requestId)
                .update("status", "rejected")
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to reject request"))
        }
    }

    override fun checkFriendshipStatus(targetUid: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val uid = currentUid
            
            // Check if they are friends
            val friendDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .collection("friends")
                .document(targetUid)
                .get()
                .await()

            if (friendDoc.exists()) {
                emit(Resource.Success("friends"))
                return@flow
            }

            // Check if a pending request is sent or received
            val requestIdSent = "${uid}_$targetUid"
            val reqSent = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                .document(requestIdSent)
                .get()
                .await()

            if (reqSent.exists()) {
                val status = reqSent.getString("status") ?: "none"
                if (status == "pending") {
                    emit(Resource.Success("pending_sent"))
                    return@flow
                }
            }

            val requestIdReceived = "${targetUid}_$uid"
            val reqReceived = firestore.collection(Constants.COLLECTION_FRIEND_REQUESTS)
                .document(requestIdReceived)
                .get()
                .await()

            if (reqReceived.exists()) {
                val status = reqReceived.getString("status") ?: "none"
                if (status == "pending") {
                    emit(Resource.Success("pending_received"))
                    return@flow
                }
            }

            emit(Resource.Success("none"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to check status"))
        }
    }
}
