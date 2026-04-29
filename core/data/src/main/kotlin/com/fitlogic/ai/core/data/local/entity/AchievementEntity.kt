package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "achievements",
    indices = [Index(value = ["user_id", "unlocked_at"])],
)
data class AchievementEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int,
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
)
