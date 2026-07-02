package com.example.campusconnect.data.repository

import com.example.campusconnect.data.model.Task
import com.example.campusconnect.util.Resource
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(studentId: String): Flow<Resource<List<Task>>>
    fun addTask(task: Task): Flow<Resource<String>>
    fun updateTask(task: Task): Flow<Resource<String>>
    fun deleteTask(taskId: String): Flow<Resource<String>>
}
