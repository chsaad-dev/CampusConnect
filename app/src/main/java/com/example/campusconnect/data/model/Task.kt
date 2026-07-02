package com.example.campusconnect.data.model

data class Task(
    val id: String = "",
    val studentId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Long = 0,
    val priority: String = "Normal", // Normal, High, Urgent
    val category: String = "Assignment", // Assignment, Quiz, Project, Lab
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
