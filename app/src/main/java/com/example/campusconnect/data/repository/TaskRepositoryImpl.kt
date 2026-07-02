package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Task
import com.example.campusconnect.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaskRepository {

    override fun getTasks(studentId: String): Flow<Resource<List<Task>>> = callbackFlow {
        trySend(Resource.Loading())
        val subscription = firestore.collection("tasks")
            .whereEqualTo("studentId", studentId)
            .orderBy("dueDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching tasks"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.toObjects(Task::class.java)
                    trySend(Resource.Success(tasks))
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun addTask(task: Task): Flow<Resource<String>> = callbackFlow {
        trySend(Resource.Loading())
        try {
            val id = firestore.collection("tasks").document().id
            val newTask = task.copy(id = id)
            firestore.collection("tasks").document(id).set(newTask).await()
            trySend(Resource.Success("Task added"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to add task"))
        }
        awaitClose()
    }

    override fun updateTask(task: Task): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("tasks").document(task.id).set(task).await()
            trySend(Resource.Success("Task updated"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to update task"))
        }
        awaitClose()
    }

    override fun deleteTask(taskId: String): Flow<Resource<String>> = callbackFlow {
        try {
            firestore.collection("tasks").document(taskId).delete().await()
            trySend(Resource.Success("Task deleted"))
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Failed to delete task"))
        }
        awaitClose()
    }
}
