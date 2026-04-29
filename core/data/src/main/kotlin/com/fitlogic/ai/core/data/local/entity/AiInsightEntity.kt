package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "ai_insights",
    indices = [Index(value = ["user_id", "created_at"]), Index(value = ["type"])],
)
data class AiInsightEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    val type: String,
    val title: String,
    val body: String,
    @ColumnInfo(name = "related_workout_id")
    val relatedWorkoutId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "read_at")
    val readAt: Long? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
)
