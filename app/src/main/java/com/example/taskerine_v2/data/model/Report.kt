package com.example.taskerine_v2.data.model

data class Report(
    val id: String,
    val reportedUsername: String,
    val description: String,
    val attachmentUris: List<String> = emptyList(), // stored as comma-separated URIs
    val reporterId: String,
    val timestamp: Long = System.currentTimeMillis()
)

