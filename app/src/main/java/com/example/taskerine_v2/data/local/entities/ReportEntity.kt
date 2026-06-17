package com.example.taskerine_v2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskerine_v2.data.model.Report

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val reportedUsername: String,
    val description: String,
    val attachmentUris: String = "",   // comma-separated URIs
    val reporterId: String,
    val timestamp: Long
)

fun ReportEntity.toModel() = Report(
    id = id,
    reportedUsername = reportedUsername,
    description = description,
    attachmentUris = if (attachmentUris.isBlank()) emptyList()
    else attachmentUris.split(","),
    reporterId = reporterId,
    timestamp = timestamp
)

fun Report.toEntity() = ReportEntity(
    id = id,
    reportedUsername = reportedUsername,
    description = description,
    attachmentUris = attachmentUris.joinToString(","),
    reporterId = reporterId,
    timestamp = timestamp
)

