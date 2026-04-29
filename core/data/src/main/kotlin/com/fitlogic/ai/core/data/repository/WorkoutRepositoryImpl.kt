@file:Suppress("MaxLineLength")

package com.fitlogic.ai.core.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.fitlogic.ai.core.data.local.FitLogicDatabase
import com.fitlogic.ai.core.data.local.dao.ExercisesCatalogDao
import com.fitlogic.ai.core.data.local.dao.SetDao
import com.fitlogic.ai.core.data.local.dao.WorkoutDao
import com.fitlogic.ai.core.data.local.dao.WorkoutExerciseDao
import com.fitlogic.ai.core.data.local.entity.ExercisesCatalogEntity
import com.fitlogic.ai.core.data.local.entity.SetEntity
import com.fitlogic.ai.core.data.local.entity.WorkoutEntity
import com.fitlogic.ai.core.data.local.entity.WorkoutExerciseEntity
import com.fitlogic.ai.core.data.local.mapper.toDomain
import com.fitlogic.ai.core.data.local.session.SessionPreferences
import com.fitlogic.ai.core.domain.model.ActiveWorkoutSession
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem
import com.fitlogic.ai.core.domain.model.FinishedWorkoutSummary
import com.fitlogic.ai.core.domain.model.Workout
import com.fitlogic.ai.core.domain.model.WorkoutDetail
import com.fitlogic.ai.core.domain.model.WorkoutExercise
import com.fitlogic.ai.core.domain.model.WorkoutExerciseWithSets
import com.fitlogic.ai.core.domain.model.WorkoutHistoryEntry
import com.fitlogic.ai.core.domain.model.WorkoutSet
import com.fitlogic.ai.core.domain.model.WorkoutSetReference
import com.fitlogic.ai.core.domain.repository.WorkoutRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class WorkoutRepositoryImpl
    @Inject
    constructor(
        private val database: FitLogicDatabase,
        private val workoutDao: WorkoutDao,
        private val workoutExerciseDao: WorkoutExerciseDao,
        private val setDao: SetDao,
        private val exercisesCatalogDao: ExercisesCatalogDao,
        private val sessionPreferences: SessionPreferences,
        @ApplicationContext private val context: Context,
    ) : WorkoutRepository {
        override fun observeActiveWorkout(): Flow<ActiveWorkoutSession?> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf(null)
                workoutDao.observeActiveWorkout(userId).flatMapLatest { workoutEntity ->
                    if (workoutEntity == null) {
                        flowOf(null)
                    } else {
                        combine(
                            workoutExerciseDao.observeByWorkoutId(workoutEntity.id),
                            setDao.observeByWorkoutId(workoutEntity.id),
                        ) { exercises, sets ->
                            exercises to sets
                        }.mapLatest { (exercises, sets) ->
                            workoutEntity.toActiveSession(
                                exercises = exercises,
                                sets = sets,
                                userId = userId,
                            )
                        }
                    }
                }
            }

        override fun observeWorkoutHistory(limit: Int): Flow<List<WorkoutHistoryEntry>> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf(emptyList())
                workoutDao.observeHistory(userId, limit).mapLatest { workouts ->
                    workouts.map { workout ->
                        val setCount = setDao.getByWorkoutId(workout.id).size
                        WorkoutHistoryEntry(
                            workoutId = workout.id,
                            title = workout.title,
                            startedAt = workout.startedAt,
                            finishedAt = workout.finishedAt,
                            totalVolume = workout.totalVolume,
                            totalSets = setCount,
                        )
                    }
                }
            }

        override fun observeWorkoutDetail(workoutId: String): Flow<WorkoutDetail?> =
            workoutDao.observeById(workoutId).flatMapLatest { workout ->
                if (workout == null) {
                    flowOf(null)
                } else {
                    combine(
                        workoutExerciseDao.observeByWorkoutId(workout.id),
                        setDao.observeByWorkoutId(workout.id),
                    ) { exercises, sets ->
                        WorkoutDetail(
                            workout = workout.toDomain(),
                            exercises =
                                exercises.map { exercise ->
                                    WorkoutExerciseWithSets(
                                        exercise = exercise.toDomain(),
                                        sets = sets.filter { it.workoutExerciseId == exercise.id }.map(SetEntity::toDomain),
                                    )
                                },
                        )
                    }
                }
            }

        override suspend fun ensureCatalogSeeded(): Result<Unit> =
            runCatching {
                if (exercisesCatalogDao.count() > 0) return@runCatching
                val json = context.assets.open(SEED_FILE).bufferedReader().use { it.readText() }
                val records = jsonParser.decodeFromString<List<ExerciseSeedRecord>>(json)
                val entities =
                    records.map { record ->
                        ExercisesCatalogEntity(
                            id = record.id,
                            name = record.name,
                            muscleGroup = record.muscleGroup,
                            equipment = record.equipment,
                            difficulty = record.difficulty,
                            instructions = record.instructions,
                            instructionStepsJson = encodeStringList(record.instructionSteps),
                            commonMistakesJson = encodeStringList(record.commonMistakes),
                            alternativeExerciseIdsJson = encodeStringList(record.alternativeExerciseIds),
                            gifAssetPath = record.gifAssetPath,
                        )
                    }
                exercisesCatalogDao.insertAll(entities)
            }

        override suspend fun searchExercises(
            query: String,
            muscleGroups: List<String>,
            equipments: List<String>,
            difficulty: String?,
            limit: Int,
        ): Result<List<ExerciseCatalogItem>> =
            runCatching {
                ensureCatalogSeeded().getOrThrow()
                val normalizedMuscleGroups = normalizeFilterValues(muscleGroups)
                val normalizedEquipments = normalizeFilterValues(equipments)
                val normalizedDifficulty = difficulty?.trim().orEmpty()
                exercisesCatalogDao.search(
                    query = query.trim(),
                    muscleGroups = normalizedMuscleGroups,
                    equipments = normalizedEquipments,
                    difficulty = normalizedDifficulty,
                    limit = limit,
                ).map(ExercisesCatalogEntity::toDomain)
            }

        override suspend fun getExerciseDetail(exerciseId: String): Result<ExerciseCatalogItem> =
            runCatching {
                ensureCatalogSeeded().getOrThrow()
                val entity = checkNotNull(exercisesCatalogDao.getById(exerciseId)) { "Egzersiz bulunamadi." }
                entity.toDomain()
            }

        override suspend fun startWorkoutEmpty(title: String?): Result<Workout> =
            runCatching {
                val userId = requireCurrentUserId()
                database.withTransaction {
                    val activeWorkout = workoutDao.getActiveWorkout(userId)
                    check(activeWorkout == null) { "Zaten aktif bir antrenman var." }
                    val now = System.currentTimeMillis()
                    val resolvedTitle = title?.takeIf { it.isNotBlank() } ?: "Bos antrenman"
                    val workout =
                        WorkoutEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            title = resolvedTitle,
                            startedAt = now,
                            createdAt = now,
                            updatedAt = now,
                        )
                    workoutDao.insert(workout)
                    workout.toDomain()
                }
            }

        override suspend fun startWorkoutFromTemplate(templateName: String): Result<Workout> =
            startWorkoutEmpty(title = "Sablon: ${templateName.ifBlank { "Genel" }}")

        override suspend fun startWorkoutFromHistory(sourceWorkoutId: String): Result<Workout> =
            runCatching {
                val userId = requireCurrentUserId()
                database.withTransaction {
                    val activeWorkout = workoutDao.getActiveWorkout(userId)
                    check(activeWorkout == null) { "Zaten aktif bir antrenman var." }

                    val sourceWorkout = workoutDao.getById(sourceWorkoutId)
                    checkNotNull(sourceWorkout) { "Tekrarlanacak antrenman bulunamadi." }
                    val sourceExercises = workoutExerciseDao.getByWorkoutId(sourceWorkoutId)
                    check(sourceExercises.isNotEmpty()) { "Tekrarlanacak antrenman egzersiz icermiyor." }

                    val now = System.currentTimeMillis()
                    val newWorkoutId = UUID.randomUUID().toString()
                    val newWorkout =
                        WorkoutEntity(
                            id = newWorkoutId,
                            userId = userId,
                            title = "Tekrar: ${sourceWorkout.title}",
                            startedAt = now,
                            createdAt = now,
                            updatedAt = now,
                        )
                    workoutDao.insert(newWorkout)

                    sourceExercises.forEachIndexed { index, item ->
                        workoutExerciseDao.insert(
                            WorkoutExerciseEntity(
                                id = UUID.randomUUID().toString(),
                                workoutId = newWorkoutId,
                                exerciseCatalogId = item.exerciseCatalogId,
                                exerciseName = item.exerciseName,
                                orderInWorkout = index,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        )
                    }
                    newWorkout.toDomain()
                }
            }

        override suspend fun addExerciseToActiveWorkout(exerciseCatalogId: String): Result<WorkoutExercise> =
            runCatching {
                ensureCatalogSeeded().getOrThrow()
                val userId = requireCurrentUserId()
                database.withTransaction {
                    val activeWorkout = checkNotNull(workoutDao.getActiveWorkout(userId)) { "Aktif antrenman bulunamadi." }
                    val exercise =
                        checkNotNull(exercisesCatalogDao.getById(exerciseCatalogId)) {
                            "Egzersiz katalogda bulunamadi."
                        }
                    val nextOrder = workoutExerciseDao.getMaxOrder(activeWorkout.id) + 1
                    val now = System.currentTimeMillis()
                    val entity =
                        WorkoutExerciseEntity(
                            id = UUID.randomUUID().toString(),
                            workoutId = activeWorkout.id,
                            exerciseCatalogId = exercise.id,
                            exerciseName = exercise.name,
                            orderInWorkout = nextOrder,
                            createdAt = now,
                            updatedAt = now,
                        )
                    workoutExerciseDao.insert(entity)
                    entity.toDomain()
                }
            }

        override suspend fun saveSet(
            workoutExerciseId: String,
            weightKg: Float,
            reps: Int,
        ): Result<WorkoutSet> =
            runCatching {
                check(weightKg > 0f) { "Agirlik sifirdan buyuk olmali." }
                check(reps > 0) { "Tekrar sayisi sifirdan buyuk olmali." }

                val userId = requireCurrentUserId()
                database.withTransaction {
                    val workoutExercise =
                        checkNotNull(workoutExerciseDao.getById(workoutExerciseId)) {
                            "Egzersiz satiri bulunamadi."
                        }
                    val workout =
                        checkNotNull(workoutDao.getById(workoutExercise.workoutId)) {
                            "Antrenman bulunamadi."
                        }
                    check(workout.status == "ACTIVE") { "Sadece aktif antrenmana set eklenebilir." }

                    val volume = weightKg * reps
                    val bestSet = setDao.getBestFinishedSetForExercise(userId, workoutExercise.exerciseCatalogId)
                    val isPr = bestSet == null || volume > bestSet.volume
                    val now = System.currentTimeMillis()
                    val setEntity =
                        SetEntity(
                            id = UUID.randomUUID().toString(),
                            workoutId = workout.id,
                            workoutExerciseId = workoutExercise.id,
                            exerciseCatalogId = workoutExercise.exerciseCatalogId,
                            weightKg = weightKg,
                            reps = reps,
                            volume = volume,
                            isPr = isPr,
                            performedAt = now,
                            createdAt = now,
                            updatedAt = now,
                        )
                    setDao.insert(setEntity)

                    val totalVolume = setDao.getByWorkoutId(workout.id).sumOf { it.volume.toDouble() }.toFloat()
                    workoutDao.updateTotalVolume(
                        workoutId = workout.id,
                        totalVolume = totalVolume,
                        updatedAt = now,
                    )
                    setEntity.toDomain()
                }
            }

        override suspend fun copyLastSet(workoutExerciseId: String): Result<WorkoutSet> =
            runCatching {
                val lastSet = setDao.getByWorkoutExerciseId(workoutExerciseId).lastOrNull()
                checkNotNull(lastSet) { "Kopyalanacak onceki set bulunamadi." }
                saveSet(
                    workoutExerciseId = workoutExerciseId,
                    weightKg = lastSet.weightKg,
                    reps = lastSet.reps,
                ).getOrThrow()
            }

        override suspend fun finishActiveWorkout(): Result<FinishedWorkoutSummary> =
            runCatching {
                val userId = requireCurrentUserId()
                database.withTransaction {
                    val activeWorkout = checkNotNull(workoutDao.getActiveWorkout(userId)) { "Aktif antrenman bulunamadi." }
                    val now = System.currentTimeMillis()
                    val sets = setDao.getByWorkoutId(activeWorkout.id)
                    val totalVolume = sets.sumOf { it.volume.toDouble() }.toFloat()
                    workoutDao.updateTotalVolume(activeWorkout.id, totalVolume, now)
                    workoutDao.markFinished(activeWorkout.id, finishedAt = now, updatedAt = now)
                    FinishedWorkoutSummary(
                        workoutId = activeWorkout.id,
                        totalVolume = totalVolume,
                        totalSets = sets.size,
                        durationMinutes = ((now - activeWorkout.startedAt) / 60_000L).coerceAtLeast(1L),
                        prCount = sets.count { it.isPr },
                    )
                }
            }

        private suspend fun requireCurrentUserId(): String {
            val snapshot = sessionPreferences.sessionSnapshot.first()
            return checkNotNull(snapshot.currentUserId) {
                "Aktif kullanici bulunamadi. Once giris yapin veya misafir modunu acin."
            }
        }

        private suspend fun WorkoutEntity.toActiveSession(
            exercises: List<WorkoutExerciseEntity>,
            sets: List<SetEntity>,
            userId: String,
        ): ActiveWorkoutSession {
            val exerciseBlocks =
                exercises.map { exercise ->
                    val previous = setDao.getLastFinishedSetForExercise(userId, exercise.exerciseCatalogId)
                    WorkoutExerciseWithSets(
                        exercise = exercise.toDomain(),
                        sets = sets.filter { it.workoutExerciseId == exercise.id }.map(SetEntity::toDomain),
                        previousReference =
                            previous?.let {
                                WorkoutSetReference(
                                    weightKg = it.weightKg,
                                    reps = it.reps,
                                    performedAt = it.performedAt,
                                )
                            },
                    )
                }
            return ActiveWorkoutSession(
                workout = this.toDomain(),
                exercises = exerciseBlocks,
            )
        }

        @Serializable
        private data class ExerciseSeedRecord(
            val id: String,
            val name: String,
            val muscleGroup: String,
            val equipment: String,
            val difficulty: String,
            val instructions: String,
            val instructionSteps: List<String>,
            val commonMistakes: List<String>,
            val alternativeExerciseIds: List<String>,
            val gifAssetPath: String,
        )

        companion object {
            private const val SEED_FILE = "workout_exercises_seed.json"
            private val defaultFilterSentinel = listOf("__all__")
            private val jsonParser = Json { ignoreUnknownKeys = true }
        }

        private fun normalizeFilterValues(values: List<String>): List<String> {
            val normalized = values.map { it.trim() }.filter { it.isNotEmpty() }
            return if (normalized.isEmpty()) defaultFilterSentinel else normalized
        }

        private fun encodeStringList(values: List<String>): String {
            return jsonParser.encodeToString(ListSerializer(String.serializer()), values)
        }
    }
