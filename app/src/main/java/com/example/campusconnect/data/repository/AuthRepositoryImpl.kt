package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.User
import com.example.campusconnect.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private var currentUser: User? = null

    override fun login(email: String, password: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            val user = document.toObject(User::class.java)
                            if (user != null) {
                                currentUser = user
                                trySend(Resource.Success(user))
                            } else {
                                trySend(Resource.Error("User data not found"))
                            }
                        }
                        .addOnFailureListener {
                            trySend(Resource.Error(it.message ?: "Failed to fetch user data"))
                        }
                }
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.message ?: "Login failed"))
            }
        awaitClose()
    }

    override fun register(user: User, password: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())
        auth.createUserWithEmailAndPassword(user.email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val newUser = user.copy(uid = uid)
                    firestore.collection("users").document(uid).set(newUser)
                        .addOnSuccessListener {
                            currentUser = newUser
                            trySend(Resource.Success(newUser))
                        }
                        .addOnFailureListener {
                            trySend(Resource.Error(it.message ?: "Failed to save user data"))
                        }
                }
            }
            .addOnFailureListener {
                trySend(Resource.Error(it.message ?: "Registration failed"))
            }
        awaitClose()
    }

    override fun logout() {
        auth.signOut()
        currentUser = null
    }

    override fun getCurrentUser(): User? {
        return currentUser
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun sendPasswordResetEmail(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            auth.sendPasswordResetEmail(email).await()
            emit(Resource.Success("Password reset email sent successfully"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to send reset email"))
        }
    }

    override suspend fun loadCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)
            currentUser = user
            user
        } catch (e: Exception) {
            null
        }
    }
}
