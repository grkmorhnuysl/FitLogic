package com.fitlogic.ai.core.domain.repository

import com.fitlogic.ai.core.domain.model.ActiveWorkoutSession
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem
import com.fitlogic.ai.core.domain.model.FinishedWorkoutSummary
import com.fitlogic.ai.core.domain.model.Workout
import com.fitlogic.ai.core.domain.model.WorkoutDetail
import com.fitlogic.ai.core.domain.model.WorkoutExercise
import com.fitlogic.ai.core.domain.model.WorkoutHistoryEntry
import com.fitlogic.ai.core.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeActiveWorkout(): Flow<ActiveWorkoutSession?>

    fun observeWorkoutHistory(limit: Int = 50): Flow<List<WorkoutHistoryEntry>>

    fun observeWorkoutDetail(workoutId: String): Flow<WorkoutDetail?>

    suspend fun ensureCatalogSeeded(): Result<Unit>

    suspend fun searchExercises(
        query: String,
        muscleGroups: List<String> = emptyList(),
        equipments: List<String> = emptyList(),
        difficulty: String? = null,
        limit: Int = 20,
    ): Result<List<ExerciseCatalogItem>>

    suspend fun getExerciseDetail(exerciseId: String): Result<ExerciseCatalogItem>

    suspend fun startWorkoutEmpty(title: String? = null): Result<Workout>

    suspend fun startWorkoutFromTemplate(templateName: String): Result<Workout>

    suspend fun startWorkoutFromHistory(sourceWorkoutId: String): Result<Workout>

    suspend fun addExerciseToActiveWorkout(exerciseCatalogId: String): Result<WorkoutExercise>

    suspend fun saveSet(
        workoutExerciseId: String,
        weightKg: Float,
        reps: Int,
    ): Result<WorkoutSet>

    suspend fun copyLastSet(workoutExerciseId: String): Result<WorkoutSet>

    suspend fun finishActiveWorkout(): Result<FinishedWorkoutSummary>
}
