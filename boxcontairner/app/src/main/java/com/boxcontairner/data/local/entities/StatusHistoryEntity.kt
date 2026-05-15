package com.boxcontairner.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.boxcontairner.domain.model.StatusHistory

@Entity(
    tableName = "status_history",
    indices = [Index(value = ["containerId"]), Index(value = ["timestamp"])]
)
data class StatusHistoryEntity(
    @PrimaryKey val id: String,
    val containerId: String,
    val action: String = "STATUS_CHANGE",
    val previousStatus: String,
    val newStatus: String,
    val changedBy: String,
    val timestamp: Long,
    val notes: String = ""
)

fun StatusHistoryEntity.toDomain() = StatusHistory(
    id = id, containerId = containerId, action = action,
    previousStatus = previousStatus, newStatus = newStatus,
    changedBy = changedBy, timestamp = timestamp, notes = notes
)

fun StatusHistory.toEntity() = StatusHistoryEntity(
    id = id, containerId = containerId, action = action,
    previousStatus = previousStatus, newStatus = newStatus,
    changedBy = changedBy, timestamp = timestamp, notes = notes
)
