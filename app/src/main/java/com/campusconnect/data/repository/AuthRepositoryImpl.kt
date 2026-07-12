package com.campusconnect.data.repository

import com.campusconnect.core.common.Constants
import com.campusconnect.core.common.Resource
import com.campusconnect.data.remote.dto.UserDto
import com.campusconnect.domain.model.User
import com.campusconnect.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Login failed: no user ID")

            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            val userDto = doc.toObject(UserDto::class.java)
            if (userDto != null) {
                emit(Resource.Success(userDto.toDomain()))
            } else {
                // User auth exists but no Firestore profile — needs profile completion
                emit(Resource.Success(User(uid = uid, email = email)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Login failed"))
        }
    }

    override fun register(email: String, password: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Registration failed: no user ID")
            emit(Resource.Success(uid))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Registration failed"))
        }
    }

    override fun sendVerificationEmail(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            auth.currentUser?.sendEmailVerification()?.await()
                ?: throw Exception("No authenticated user")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send verification email"))
        }
    }

    override fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()
        return auth.currentUser?.isEmailVerified == true
    }

    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            auth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send reset email"))
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    override fun loginWithGoogle(idToken: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: throw Exception("Google login failed: no user ID")
            val email = result.user?.email ?: ""

            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .get()
                .await()

            val userDto = doc.toObject(UserDto::class.java)
            if (userDto != null) {
                emit(Resource.Success(userDto.toDomain()))
            } else {
                // User auth exists but no Firestore profile — needs profile completion
                emit(Resource.Success(User(uid = uid, email = email)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Google login failed"))
        }
    }
}
