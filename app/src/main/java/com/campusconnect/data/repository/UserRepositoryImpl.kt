package com.campusconnect.data.repository

import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.data.remote.dto.UserDto
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUserById(uid: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            val userDto = doc.toObject(UserDto::class.java)
            if (userDto != null) {
                emit(Resource.Success(userDto.toDomain()))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch user"))
        }
    }

    override fun getCurrentUserProfile(): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(Resource.Error("Not logged in"))
            return@flow
        }
        try {
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            val userDto = doc.toObject(UserDto::class.java)
            if (userDto != null) {
                emit(Resource.Success(userDto.toDomain()))
            } else {
                emit(Resource.Error("Profile not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch profile"))
        }
    }

    override fun updateProfile(user: User): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val dto = UserDto.fromDomain(user)
            firestore.collection(Constants.COLLECTION_USERS)
                .document(user.uid)
                .set(dto)
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update profile"))
        }
    }

    override fun checkUsernameAvailability(username: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_USERNAMES)
                .document(username.lowercase())
                .get()
                .await()
            // Available if document doesn't exist
            emit(Resource.Success(!doc.exists()))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to check username"))
        }
    }

    override fun claimUsername(uid: String, username: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            firestore.collection(Constants.COLLECTION_USERNAMES)
                .document(username.lowercase())
                .set(mapOf(Constants.FIELD_UID to uid))
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to claim username"))
        }
    }

    override fun completeProfile(user: User): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val dto = UserDto.fromDomain(user.copy(profileComplete = true))
            val batch = firestore.batch()

            // Write user document
            val userRef = firestore.collection(Constants.COLLECTION_USERS).document(user.uid)
            batch.set(userRef, dto)

            // Claim username
            val usernameRef = firestore.collection(Constants.COLLECTION_USERNAMES)
                .document(user.uniqueUsername.lowercase())
            batch.set(usernameRef, mapOf(Constants.FIELD_UID to user.uid))

            batch.commit().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to complete profile"))
        }
    }

    override fun isProfileComplete(uid: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            if (!doc.exists()) {
                emit(Resource.Success(false))
                return@flow
            }

            val isComplete = doc.getBoolean("profileComplete") ?: false
            emit(Resource.Success(isComplete))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to check profile status"))
        }
    }

    override fun trackSubjectView(subject: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val uid = auth.currentUser?.uid
            if (uid != null && subject.isNotBlank()) {
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .update("viewedSubjects", com.google.firebase.firestore.FieldValue.arrayUnion(subject.trim()))
                    .await()
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to track subject view"))
        }
    }
}
