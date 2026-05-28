package com.example.taskerine_v2.data.model

data class Review(
    val id: String,
    val taskId: String,
    val taskTitle: String,
    val reviewerId: String,
    val reviewerName: String,
    val targetUserId: String,
    val rating: Int,        // 1-5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
