package com.example.taskerine_v2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskerine_v2.data.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val location: String,
    val reward: Double,
    val requesterId: String,
    val requesterName: String,
    val status: TaskStatus = TaskStatus.OPEN,
    val acceptedById: String? = null
)

