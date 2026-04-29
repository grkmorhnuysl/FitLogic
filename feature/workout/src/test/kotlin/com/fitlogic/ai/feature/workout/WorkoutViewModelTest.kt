package com.fitlogic.ai.feature.workout

import com.fitlogic.ai.core.domain.model.ActiveWorkoutSession
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem
import com.fitlogic.ai.core.domain.model.FinishedWorkoutSummary
import com.fitlogic.ai.core.domain.model.Workout
import com.fitlogic.ai.core.domain.model.WorkoutDetail
import com.fitlogic.ai.core.domain.model.WorkoutExercise
import com.fitlogic.ai.core.domain.model.WorkoutExerciseWithSets
import com.fitlogic.ai.core.domain.model.WorkoutHistoryEntry
import com.fitlogic.ai.core.domain.model.WorkoutSet
import com.fitlogic.ai.core.domain.model.WorkoutStatus
import com.fitlogic.ai.core.domain.repository.WorkoutRepository
import com.fitlogic.ai.core.domain.usecase.workout.AddExerciseToWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.CopyLastSetUseCase
import com.fitlogic.ai.core.domain.usecase.workout.EnsureExerciseCatalogSeededUseCase
import com.fitlogic.ai.core.domain.usecase.workout.FinishWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.GetWorkoutHistoryUseCase
import com.fitlogic.ai.core.domain.usecase.workout.ObserveActiveWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.ObserveWorkoutDetailUseCase
import com.fitlogic.ai.core.domain.usecase.workout.SaveSetUseCase
import com.fitlogic.ai.core.domain.usecase.workout.SearchExercisesUseCase
import com.fitlogic.ai.core.domain.usecase.workout.StartWorkoutFromHistoryUseCase
import com.fitlogic.ai.core.domain.usecase.workout.StartWorkoutFromTemplateUseCase
import com.fitlogic.ai.core.domain.usecase.workout.StartWorkoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeWorkoutRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeWorkoutRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startEmptyWorkout_createsActiveSession() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)

            viewModel.startEmptyWorkout()
            runCurrent()

            assertNotNull(viewModel.uiState.value.activeSession)
        }

    @Test
    fun saveSet_withInvalidInput_setsErrorMessage() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)

            viewModel.saveSet(
                workoutExerciseId = "exercise-1",
                weightRaw = "abc",
                repsRaw = "10",
            )
            runCurrent()

            assertTrue(viewModel.uiState.value.message?.contains("Gecerli agirlik") == true)
        }

    private fun buildViewModel(fakeRepository: WorkoutRepository): WorkoutViewModel =
        WorkoutViewModel(
            observeActiveWorkoutUseCase = ObserveActiveWorkoutUseCase(fakeRepository),
            getWorkoutHistoryUseCase = GetWorkoutHistoryUseCase(fakeRepository),
            observeWorkoutDetailUseCase = ObserveWorkoutDetailUseCase(fakeRepository),
            ensureExerciseCatalogSeededUseCase = EnsureExerciseCatalogSeededUseCase(fakeRepository),
            searchExercisesUseCase = SearchExercisesUseCase(fakeRepository),
            startWorkoutUseCase = StartWorkoutUseCase(fakeRepository),
            startWorkoutFromTemplateUseCase = StartWorkoutFromTemplateUseCase(fakeRepository),
            startWorkoutFromHistoryUseCase = StartWorkoutFromHistoryUseCase(fakeRepository),
            addExerciseToWorkoutUseCase = AddExerciseToWorkoutUseCase(fakeRepository),
            saveSetUseCase = SaveSetUseCase(fakeRepository),
            copyLastSetUseCase = CopyLastSetUseCase(fakeRepository),
            finishWorkoutUseCase = FinishWorkoutUseCase(fakeRepository),
        )

    private class FakeWorkoutRepository : WorkoutRepository {
        private val active = MutableStateFlow<ActiveWorkoutSession?>(null)
        private val history = MutableStateFlow<List<WorkoutHistoryEntry>>(emptyList())

        override fun observeActiveWorkout(): Flow<ActiveWorkoutSession?> = active

        override fun observeWorkoutHistory(limit: Int): Flow<List<WorkoutHistoryEntry>> = history

        override fun observeWorkoutDetail(workoutId: String): Flow<WorkoutDetail?> = flowOf(null)

        override suspend fun ensureCatalogSeeded(): Result<Unit> = Result.success(Unit)

        override suspend fun searchExercises(
            query: String,
            muscleGroups: List<String>,
            equipments: List<String>,
            difficulty: String?,
            limit: Int,
        ): Result<List<ExerciseCatalogItem>> =
            Result.success(
                listOf(
                    ExerciseCatalogItem(
                        id = "ex-1",
                        name = "Bench Press",
                        muscleGroup = "Chest",
                        equipment = "Barbell",
                        difficulty = "Beginner",
                        instructions = "",
                    ),
                ),
            )

        override suspend fun getExerciseDetail(exerciseId: String): Result<ExerciseCatalogItem> =
            Result.success(
                ExerciseCatalogItem(
                    id = exerciseId,
                    name = "Bench Press",
                    muscleGroup = "Chest",
                    equipment = "Barbell",
                    difficulty = "Beginner",
                    instructions = "",
                ),
            )

        override suspend fun startWorkoutEmpty(title: String?): Result<Workout> {
            val workout =
                Workout(
                    id = "w-1",
                    userId = "u-1",
                    title = title ?: "Bos antrenman",
                    status = WorkoutStatus.ACTIVE,
                )
            active.value = ActiveWorkoutSession(workout = workout, exercises = emptyList())
            return Result.success(workout)
        }

        override suspend fun startWorkoutFromTemplate(templateName: String): Result<Workout> = startWorkoutEmpty(templateName)

        override suspend fun startWorkoutFromHistory(sourceWorkoutId: String): Result<Workout> = startWorkoutEmpty("Tekrar")

        override suspend fun addExerciseToActiveWorkout(exerciseCatalogId: String): Result<WorkoutExercise> {
            val activeWorkout =
                active.value?.workout ?: return Result.failure(IllegalStateException("No active"))
            val exercise =
                WorkoutExercise(
                    id = "exercise-1",
                    workoutId = activeWorkout.id,
                    exerciseCatalogId = exerciseCatalogId,
                    exerciseName = "Bench Press",
                    orderInWorkout = 0,
                )
            active.value =
                ActiveWorkoutSession(
                    workout = activeWorkout,
                    exercises = listOf(WorkoutExerciseWithSets(exercise = exercise, sets = emptyList())),
                )
            return Result.success(exercise)
        }

        override suspend fun saveSet(
            workoutExerciseId: String,
            weightKg: Float,
            reps: Int,
        ): Result<WorkoutSet> =
            Result.success(
                WorkoutSet(
                    id = "set-1",
                    workoutId = "w-1",
                    workoutExerciseId = workoutExerciseId,
                    exerciseCatalogId = "ex-1",
                    weightKg = weightKg,
                    reps = reps,
                    volume = weightKg * reps,
                    isPr = true,
                ),
            )

        override suspend fun copyLastSet(workoutExerciseId: String): Result<WorkoutSet> = saveSet(workoutExerciseId, 40f, 8)

        override suspend fun finishActiveWorkout(): Result<FinishedWorkoutSummary> =
            Result.success(
                FinishedWorkoutSummary(
                    workoutId = "w-1",
                    totalVolume = 320f,
                    totalSets = 1,
                    durationMinutes = 10,
                    prCount = 1,
                ),
            )
    }
}
