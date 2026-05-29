package com.example.taskerine_v2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val taskTitle: String,
    val reviewerId: String,
    val reviewerName: String,
    val targetUserId: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

