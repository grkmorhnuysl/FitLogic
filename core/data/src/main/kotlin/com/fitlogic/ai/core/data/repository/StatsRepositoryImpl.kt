package com.fitlogic.ai.core.data.repository

import com.fitlogic.ai.core.data.local.dao.BodyWeightEntryDao
import com.fitlogic.ai.core.data.local.dao.SetDao
import com.fitlogic.ai.core.data.local.dao.WorkoutDao
import com.fitlogic.ai.core.data.local.entity.BodyWeightEntryEntity
import com.fitlogic.ai.core.data.local.session.SessionPreferences
import com.fitlogic.ai.core.domain.model.ExerciseProgressPoint
import com.fitlogic.ai.core.domain.model.MuscleGroupDistributionPoint
import com.fitlogic.ai.core.domain.model.PrHistoryPoint
import com.fitlogic.ai.core.domain.model.WeeklyStatsSummary
import com.fitlogic.ai.core.domain.model.WeeklyVolumePoint
import com.fitlogic.ai.core.domain.model.WeightTrendPoint
import com.fitlogic.ai.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepositoryImpl
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
        private val setDao: SetDao,
        private val bodyWeightEntryDao: BodyWeightEntryDao,
        private val sessionPreferences: SessionPreferences,
    ) : StatsRepository {
        override fun observeWeeklySummary(): Flow<WeeklyStatsSummary> =
            withUserId { userId ->
                val weekStart = System.currentTimeMillis() - WEEK_MILLIS
                combine(
                    workoutDao.observeWeeklySummary(userId = userId, fromEpochMs = weekStart),
                    setDao.observeWeeklyPrCount(userId = userId, fromEpochMs = weekStart),
                ) { summary, prCount ->
                    WeeklyStatsSummary(
                        workoutsCompleted = summary.workoutsCompleted,
                        totalVolume = summary.totalVolume,
                        prCount = prCount,
                        avgWorkoutMinutes = summary.avgDurationMinutes.coerceAtLeast(0L),
                    )
                }
            }

        override fun observeExerciseProgress(exerciseCatalogId: String): Flow<List<ExerciseProgressPoint>> =
            withUserId { userId ->
                setDao.observeExerciseProgress(
                    userId = userId,
                    exerciseCatalogId = exerciseCatalogId,
                ).map { rows ->
                    rows.map {
                        ExerciseProgressPoint(
                            dateEpochMs = it.dateEpochMs,
                            totalVolume = it.totalVolume,
                            bestWeightKg = it.bestWeightKg,
                        )
                    }
                }
            }

        override fun observeWeeklyVolume(weekCount: Int): Flow<List<WeeklyVolumePoint>> =
            withUserId { userId ->
                workoutDao.observeWeeklyVolume(
                    userId = userId,
                    weekCount = weekCount,
                ).map { rows ->
                    rows.map {
                        WeeklyVolumePoint(
                            weekLabel = it.weekLabel,
                            totalVolume = it.totalVolume,
                        )
                    }.reversed()
                }
            }

        override fun observeMuscleGroupDistribution(): Flow<List<MuscleGroupDistributionPoint>> =
            withUserId { userId ->
                setDao.observeMuscleDistribution(userId).map { rows ->
                    rows.map {
                        MuscleGroupDistributionPoint(
                            muscleGroup = it.muscleGroup,
                            totalVolume = it.totalVolume,
                        )
                    }
                }
            }

        override fun observePrHistory(limit: Int): Flow<List<PrHistoryPoint>> =
            withUserId { userId ->
                setDao.observePrHistory(userId = userId, limit = limit).map { rows ->
                    rows.map {
                        PrHistoryPoint(
                            performedAt = it.performedAt,
                            exerciseName = it.exerciseName,
                            weightKg = it.weightKg,
                            reps = it.reps,
                            volume = it.volume,
                        )
                    }
                }
            }

        override fun observeWeightTrend(limit: Int): Flow<List<WeightTrendPoint>> =
            withUserId { userId ->
                bodyWeightEntryDao.observeByUserId(userId = userId, limit = limit).map { rows ->
                    rows.map {
                        WeightTrendPoint(
                            measuredAt = it.measuredAt,
                            weightKg = it.weightKg,
                        )
                    }.reversed()
                }
            }

        override suspend fun addWeightEntry(
            weightKg: Float,
            measuredAt: Long,
        ): Result<Unit> =
            runCatching {
                check(weightKg > 0f) { "Kilo sifirdan buyuk olmali." }
                val userId = requireCurrentUserId()
                val now = System.currentTimeMillis()
                bodyWeightEntryDao.insert(
                    BodyWeightEntryEntity(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        weightKg = weightKg,
                        measuredAt = measuredAt,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            }

        private fun <T> withUserId(source: (String) -> Flow<T>): Flow<T> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf()
                source(userId)
            }

        private suspend fun requireCurrentUserId(): String {
            val snapshot = sessionPreferences.sessionSnapshot.first()
            return checkNotNull(snapshot.currentUserId) { "Aktif kullanici bulunamadi." }
        }

        companion object {
            private const val WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L
        }
    }
