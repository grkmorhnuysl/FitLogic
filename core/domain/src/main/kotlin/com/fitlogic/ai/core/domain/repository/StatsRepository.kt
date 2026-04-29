package com.fitlogic.ai.core.domain.repository

import com.fitlogic.ai.core.domain.model.ExerciseProgressPoint
import com.fitlogic.ai.core.domain.model.MuscleGroupDistributionPoint
import com.fitlogic.ai.core.domain.model.PrHistoryPoint
import com.fitlogic.ai.core.domain.model.WeeklyStatsSummary
import com.fitlogic.ai.core.domain.model.WeeklyVolumePoint
import com.fitlogic.ai.core.domain.model.WeightTrendPoint
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun observeWeeklySummary(): Flow<WeeklyStatsSummary>

    fun observeExerciseProgress(exerciseCatalogId: String): Flow<List<ExerciseProgressPoint>>

    fun observeWeeklyVolume(weekCount: Int = 8): Flow<List<WeeklyVolumePoint>>

    fun observeMuscleGroupDistribution(): Flow<List<MuscleGroupDistributionPoint>>

    fun observePrHistory(limit: Int = 30): Flow<List<PrHistoryPoint>>

    fun observeWeightTrend(limit: Int = 30): Flow<List<WeightTrendPoint>>

    suspend fun addWeightEntry(weightKg: Float, measuredAt: Long = System.currentTimeMillis()): Result<Unit>
}
