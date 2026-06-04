package com.example.taskerine_v2.data.model

data class Message(
    val id: String,
    val taskId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

