package com.example.taskerine_v2.data.local.dao

import androidx.room.*
import com.example.taskerine_v2.data.local.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun getMessagesForTask(taskId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}

