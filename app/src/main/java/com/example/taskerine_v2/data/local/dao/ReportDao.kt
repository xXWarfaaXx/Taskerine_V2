package com.example.taskerine_v2.data.local.dao

import androidx.room.*
import com.example.taskerine_v2.data.local.entities.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("SELECT * FROM reports WHERE reporterId = :userId ORDER BY timestamp DESC")
    fun getReportsForUser(userId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>
}