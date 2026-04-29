package com.fitlogic.ai.core.data.local.mapper

import com.fitlogic.ai.core.data.local.entity.ExercisesCatalogEntity
import com.fitlogic.ai.core.data.local.entity.SetEntity
import com.fitlogic.ai.core.data.local.entity.WorkoutEntity
import com.fitlogic.ai.core.data.local.entity.WorkoutExerciseEntity
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem
import com.fitlogic.ai.core.domain.model.Workout
import com.fitlogic.ai.core.domain.model.WorkoutExercise
import com.fitlogic.ai.core.domain.model.WorkoutSet
import com.fitlogic.ai.core.domain.model.WorkoutStatus
import kotlinx.serialization.json.Json

fun WorkoutEntity.toDomain(): Workout =
    Workout(
        id = id,
        userId = userId,
        title = title,
        status = runCatching { enumValueOf<WorkoutStatus>(status) }.getOrDefault(WorkoutStatus.ACTIVE),
        totalVolume = totalVolume,
        startedAt = startedAt,
        finishedAt = finishedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun WorkoutExerciseEntity.toDomain(): WorkoutExercise =
    WorkoutExercise(
        id = id,
        workoutId = workoutId,
        exerciseCatalogId = exerciseCatalogId,
        exerciseName = exerciseName,
        orderInWorkout = orderInWorkout,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun SetEntity.toDomain(): WorkoutSet =
    WorkoutSet(
        id = id,
        workoutId = workoutId,
        workoutExerciseId = workoutExerciseId,
        exerciseCatalogId = exerciseCatalogId,
        weightKg = weightKg,
        reps = reps,
        volume = volume,
        isPr = isPr,
        performedAt = performedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun ExercisesCatalogEntity.toDomain(): ExerciseCatalogItem =
    ExerciseCatalogItem(
        id = id,
        name = name,
        muscleGroup = muscleGroup,
        equipment = equipment,
        difficulty = difficulty,
        instructions = instructions,
        instructionSteps = decodeStringList(instructionStepsJson),
        commonMistakes = decodeStringList(commonMistakesJson),
        alternativeExerciseIds = decodeStringList(alternativeExerciseIdsJson),
        gifAssetPath = gifAssetPath,
    )

private val jsonParser = Json { ignoreUnknownKeys = true }

private fun decodeStringList(raw: String): List<String> =
    runCatching { jsonParser.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
