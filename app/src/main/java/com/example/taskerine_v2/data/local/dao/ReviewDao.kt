package com.example.taskerine_v2.data.local.dao

import androidx.room.*
import com.example.taskerine_v2.data.local.entities.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE targetUserId = :userId")
    fun getReviewsForUser(userId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE taskId = :taskId AND reviewerId = :reviewerId LIMIT 1")
    suspend fun getReviewForTask(taskId: String, reviewerId: String): ReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)
}

