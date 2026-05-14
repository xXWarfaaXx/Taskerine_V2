package com.example.taskerine_v2.data.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val location: String,
    val reward: Double,
    val requesterId: String,
    val requesterName: String,
    val status: TaskStatus = TaskStatus.OPEN,
    val acceptedById: String? = null
)
