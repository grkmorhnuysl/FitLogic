package com.fitlogic.ai.feature.exercises

import com.fitlogic.ai.core.domain.model.ActiveWorkoutSession
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem
import com.fitlogic.ai.core.domain.model.FinishedWorkoutSummary
import com.fitlogic.ai.core.domain.model.Workout
import com.fitlogic.ai.core.domain.model.WorkoutDetail
import com.fitlogic.ai.core.domain.model.WorkoutExercise
import com.fitlogic.ai.core.domain.model.WorkoutExerciseWithSets
import com.fitlogic.ai.core.domain.model.WorkoutHistoryEntry
import com.fitlogic.ai.core.domain.model.WorkoutSet
import com.fitlogic.ai.core.domain.repository.WorkoutRepository
import com.fitlogic.ai.core.domain.usecase.workout.AddExerciseToWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.EnsureExerciseCatalogSeededUseCase
import com.fitlogic.ai.core.domain.usecase.workout.GetExerciseDetailUseCase
import com.fitlogic.ai.core.domain.usecase.workout.ObserveActiveWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.SearchExercisesUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesViewModelTest {
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
    fun searchExercises_returnsResults() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)
            runCurrent()

            assertTrue(viewModel.uiState.value.results.isNotEmpty())
        }

    @Test
    fun onQueryChange_updatesQuery() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)
            runCurrent()

            viewModel.onQueryChange("squat")
            runCurrent()

            assertEquals("squat", viewModel.uiState.value.query)
        }

    @Test
    fun toggleMuscleGroup_updatesFilters() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)
            runCurrent()

            viewModel.toggleMuscleGroup("Chest")
            runCurrent()

            assertTrue("Chest" in viewModel.uiState.value.selectedMuscleGroups)

            viewModel.toggleMuscleGroup("Chest")
            runCurrent()

            assertTrue("Chest" !in viewModel.uiState.value.selectedMuscleGroups)
        }

    @Test
    fun setDifficulty_togglesBetweenValueAndNull() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)
            runCurrent()

            viewModel.setDifficulty("Beginner")
            runCurrent()
            assertEquals("Beginner", viewModel.uiState.value.selectedDifficulty)

            viewModel.setDifficulty("Beginner")
            runCurrent()
            assertNull(viewModel.uiState.value.selectedDifficulty)
        }

    @Test
    fun loadExerciseDetail_setsSelectedExercise() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)
            runCurrent()

            viewModel.loadExerciseDetail("ex-1")
            runCurrent()

            assertNotNull(viewModel.uiState.value.selectedExercise)
            assertEquals("ex-1", viewModel.uiState.value.selectedExercise?.id)
        }

    @Test
    fun addToWorkout_success_setsMessage() =
        runTest(dispatcher) {
            repository.activeWorkout.value =
                ActiveWorkoutSession(
                    workout = Workout(id = "w-1", userId = "u-1", title = "Test"),
                    exercises = emptyList(),
                )
            val viewModel = buildViewModel(repository)
            runCurrent()

            viewModel.addToWorkout("ex-1")
            runCurrent()

            assertNotNull(viewModel.uiState.value.message)
            assertTrue(viewModel.uiState.value.message!!.contains("eklendi"))
        }

    @Test
    fun clearMessage_removesMessage() =
        runTest(dispatcher) {
            val viewModel = buildViewModel(repository)
            runCurrent()

            viewModel.addToWorkout("ex-1")
            runCurrent()
            viewModel.clearMessage()

            assertNull(viewModel.uiState.value.message)
        }

    private fun buildViewModel(fakeRepository: WorkoutRepository): ExercisesViewModel =
        ExercisesViewModel(
            searchExercisesUseCase = SearchExercisesUseCase(fakeRepository),
            getExerciseDetailUseCase = GetExerciseDetailUseCase(fakeRepository),
            ensureExerciseCatalogSeededUseCase = EnsureExerciseCatalogSeededUseCase(fakeRepository),
            addExerciseToWorkoutUseCase = AddExerciseToWorkoutUseCase(fakeRepository),
            observeActiveWorkoutUseCase = ObserveActiveWorkoutUseCase(fakeRepository),
        )

    private class FakeWorkoutRepository : WorkoutRepository {
        val activeWorkout = MutableStateFlow<ActiveWorkoutSession?>(null)

        override fun observeActiveWorkout(): Flow<ActiveWorkoutSession?> = activeWorkout

        override fun observeWorkoutHistory(limit: Int): Flow<List<WorkoutHistoryEntry>> = flowOf(emptyList())

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

        override suspend fun startWorkoutEmpty(title: String?): Result<Workout> =
            Result.success(Workout(id = "w-1", userId = "u-1", title = title ?: "Test"))

        override suspend fun startWorkoutFromTemplate(templateName: String): Result<Workout> {
            return startWorkoutEmpty(templateName)
        }

        override suspend fun startWorkoutFromHistory(sourceWorkoutId: String): Result<Workout> {
            return startWorkoutEmpty("Tekrar")
        }

        override suspend fun addExerciseToActiveWorkout(exerciseCatalogId: String): Result<WorkoutExercise> {
            val workout = activeWorkout.value?.workout ?: Workout(id = "w-1", userId = "u-1", title = "Test")
            val exercise =
                WorkoutExercise(
                    id = "we-1",
                    workoutId = workout.id,
                    exerciseCatalogId = exerciseCatalogId,
                    exerciseName = "Bench Press",
                    orderInWorkout = 0,
                )
            activeWorkout.value =
                ActiveWorkoutSession(
                    workout = workout,
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
                    id = "s-1",
                    workoutId = "w-1",
                    workoutExerciseId = workoutExerciseId,
                    exerciseCatalogId = "ex-1",
                    weightKg = weightKg,
                    reps = reps,
                    volume = weightKg * reps,
                    isPr = false,
                ),
            )

        override suspend fun copyLastSet(workoutExerciseId: String): Result<WorkoutSet> {
            return saveSet(workoutExerciseId, 50f, 8)
        }

        override suspend fun finishActiveWorkout(): Result<FinishedWorkoutSummary> =
            Result.success(
                FinishedWorkoutSummary(
                    workoutId = "w-1",
                    totalVolume = 0f,
                    totalSets = 0,
                    durationMinutes = 0,
                    prCount = 0,
                ),
            )
    }
}
