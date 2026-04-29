package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sets",
    indices = [Index(value = ["workout_id"]), Index(value = ["workout_exercise_id", "performed_at"])],
)
data class SetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "workout_id")
    val workoutId: String,
    @ColumnInfo(name = "workout_exercise_id")
    val workoutExerciseId: String,
    @ColumnInfo(name = "exercise_catalog_id")
    val exerciseCatalogId: String,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Float,
    val reps: Int,
    val volume: Float,
    @ColumnInfo(name = "is_pr")
    val isPr: Boolean = false,
    @ColumnInfo(name = "performed_at")
    val performedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
)
