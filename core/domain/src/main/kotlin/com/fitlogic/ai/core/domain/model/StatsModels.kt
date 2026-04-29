package com.fitlogic.ai.core.domain.model

data class ExerciseProgressPoint(
    val dateEpochMs: Long,
    val totalVolume: Float,
    val bestWeightKg: Float,
)

data class WeeklyVolumePoint(
    val weekLabel: String,
    val totalVolume: Float,
)

data class MuscleGroupDistributionPoint(
    val muscleGroup: String,
    val totalVolume: Float,
)

data class PrHistoryPoint(
    val performedAt: Long,
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int,
    val volume: Float,
)

data class WeightTrendPoint(
    val measuredAt: Long,
    val weightKg: Float,
)

data class WeeklyStatsSummary(
    val workoutsCompleted: Int,
    val totalVolume: Float,
    val prCount: Int,
    val avgWorkoutMinutes: Long,
)
