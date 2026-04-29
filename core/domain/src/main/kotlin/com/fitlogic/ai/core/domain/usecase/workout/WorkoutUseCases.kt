package com.fitlogic.ai.core.domain.usecase.workout

import com.fitlogic.ai.core.domain.model.ActiveWorkoutSession
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem
import com.fitlogic.ai.core.domain.model.FinishedWorkoutSummary
import com.fitlogic.ai.core.domain.model.Workout
import com.fitlogic.ai.core.domain.model.WorkoutDetail
import com.fitlogic.ai.core.domain.model.WorkoutExercise
import com.fitlogic.ai.core.domain.model.WorkoutHistoryEntry
import com.fitlogic.ai.core.domain.model.WorkoutSet
import com.fitlogic.ai.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveWorkoutUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        operator fun invoke(): Flow<ActiveWorkoutSession?> = workoutRepository.observeActiveWorkout()
    }

class ObserveWorkoutHistoryUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        operator fun invoke(limit: Int = 50): Flow<List<WorkoutHistoryEntry>> = workoutRepository.observeWorkoutHistory(limit)
    }

class ObserveWorkoutDetailUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        operator fun invoke(workoutId: String): Flow<WorkoutDetail?> = workoutRepository.observeWorkoutDetail(workoutId)
    }

class EnsureExerciseCatalogSeededUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = workoutRepository.ensureCatalogSeeded()
    }

class SearchExercisesUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(
            query: String,
            muscleGroups: List<String> = emptyList(),
            equipments: List<String> = emptyList(),
            difficulty: String? = null,
            limit: Int = 20,
        ): Result<List<ExerciseCatalogItem>> =
            workoutRepository.searchExercises(
                query = query,
                muscleGroups = muscleGroups,
                equipments = equipments,
                difficulty = difficulty,
                limit = limit,
            )
    }

class GetExerciseDetailUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(exerciseId: String): Result<ExerciseCatalogItem> =
            workoutRepository.getExerciseDetail(exerciseId)
    }

class StartWorkoutUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(title: String? = null): Result<Workout> = workoutRepository.startWorkoutEmpty(title)
    }

class FinishWorkoutUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(): Result<FinishedWorkoutSummary> = workoutRepository.finishActiveWorkout()
    }

class AddExerciseToWorkoutUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(exerciseCatalogId: String): Result<WorkoutExercise> =
            workoutRepository.addExerciseToActiveWorkout(exerciseCatalogId)
    }

class SaveSetUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(
            workoutExerciseId: String,
            weightKg: Float,
            reps: Int,
        ): Result<WorkoutSet> = workoutRepository.saveSet(workoutExerciseId, weightKg, reps)
    }

class GetWorkoutHistoryUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        operator fun invoke(limit: Int = 50): Flow<List<WorkoutHistoryEntry>> = workoutRepository.observeWorkoutHistory(limit)
    }

class CopyLastSetUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(workoutExerciseId: String): Result<WorkoutSet> = workoutRepository.copyLastSet(workoutExerciseId)
    }

class StartWorkoutFromTemplateUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(templateName: String): Result<Workout> = workoutRepository.startWorkoutFromTemplate(templateName)
    }

class StartWorkoutFromHistoryUseCase
    @Inject
    constructor(
        private val workoutRepository: WorkoutRepository,
    ) {
        suspend operator fun invoke(sourceWorkoutId: String): Result<Workout> = workoutRepository.startWorkoutFromHistory(sourceWorkoutId)
    }
