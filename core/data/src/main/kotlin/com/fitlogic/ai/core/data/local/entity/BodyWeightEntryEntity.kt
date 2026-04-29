package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "body_weight_entries",
    indices = [Index(value = ["user_id", "measured_at"])],
)
data class BodyWeightEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Float,
    @ColumnInfo(name = "measured_at")
    val measuredAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
)
