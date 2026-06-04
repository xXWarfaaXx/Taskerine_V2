package com.example.taskerine_v2.data.local

import com.example.taskerine_v2.data.local.entities.MessageEntity
import com.example.taskerine_v2.data.local.entities.ReviewEntity
import com.example.taskerine_v2.data.local.entities.TaskEntity
import com.example.taskerine_v2.data.local.entities.UserEntity
import com.example.taskerine_v2.data.model.Message
import com.example.taskerine_v2.data.model.Review
import com.example.taskerine_v2.data.model.Task
import com.example.taskerine_v2.data.model.User

fun TaskEntity.toModel() = Task(id, title, description, location, reward, requesterId, requesterName, status, acceptedById)
fun Task.toEntity() = TaskEntity(id, title, description, location, reward, requesterId, requesterName, status, acceptedById)

fun UserEntity.toModel() = User(id, username, email, role, coins)
fun User.toEntity() = UserEntity(id, username, email, role, coins)

fun ReviewEntity.toModel() = Review(id, taskId, taskTitle, reviewerId, reviewerName, targetUserId, rating, comment, timestamp)
fun Review.toEntity() = ReviewEntity(id, taskId, taskTitle, reviewerId, reviewerName, targetUserId, rating, comment, timestamp)

fun MessageEntity.toModel() = Message(id, taskId, senderId, senderName, content, timestamp)
fun Message.toEntity() = MessageEntity(id, taskId, senderId, senderName, content, timestamp)

