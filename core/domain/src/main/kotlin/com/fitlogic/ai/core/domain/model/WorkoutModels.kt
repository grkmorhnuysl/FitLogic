package com.fitlogic.ai.core.domain.model

import java.util.UUID

enum class WorkoutStatus {
    ACTIVE,
    FINISHED,
}

data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val status: WorkoutStatus = WorkoutStatus.ACTIVE,
    val totalVolume: Float = 0f,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class WorkoutExercise(
    val id: String = UUID.randomUUID().toString(),
    val workoutId: String,
    val exerciseCatalogId: String,
    val exerciseName: String,
    val orderInWorkout: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class WorkoutSet(
    val id: String = UUID.randomUUID().toString(),
    val workoutId: String,
    val workoutExerciseId: String,
    val exerciseCatalogId: String,
    val weightKg: Float,
    val reps: Int,
    val volume: Float,
    val isPr: Boolean,
    val performedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class ExerciseCatalogItem(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
    val instructionSteps: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList(),
    val alternativeExerciseIds: List<String> = emptyList(),
    val gifAssetPath: String = "",
)

data class WorkoutSetReference(
    val weightKg: Float,
    val reps: Int,
    val performedAt: Long,
)

data class WorkoutExerciseWithSets(
    val exercise: WorkoutExercise,
    val sets: List<WorkoutSet>,
    val previousReference: WorkoutSetReference? = null,
)

data class ActiveWorkoutSession(
    val workout: Workout,
    val exercises: List<WorkoutExerciseWithSets>,
)

data class FinishedWorkoutSummary(
    val workoutId: String,
    val totalVolume: Float,
    val totalSets: Int,
    val durationMinutes: Long,
    val prCount: Int,
)

data class WorkoutHistoryEntry(
    val workoutId: String,
    val title: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val totalVolume: Float,
    val totalSets: Int,
)

data class WorkoutDetail(
    val workout: Workout,
    val exercises: List<WorkoutExerciseWithSets>,
)
