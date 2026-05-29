package com.example.taskerine_v2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskerine_v2.data.model.Role

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val role: Role,
    val coins: Int = 0
)

