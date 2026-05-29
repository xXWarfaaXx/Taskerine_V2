package com.example.taskerine_v2.data.local

import androidx.room.TypeConverter
import com.example.taskerine_v2.data.model.Role
import com.example.taskerine_v2.data.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromRole(value: Role): String = value.name

    @TypeConverter
    fun toRole(value: String): Role = Role.valueOf(value)
}

