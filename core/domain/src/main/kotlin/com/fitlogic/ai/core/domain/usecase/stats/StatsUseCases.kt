package com.fitlogic.ai.core.domain.usecase.stats

import com.fitlogic.ai.core.domain.model.ExerciseProgressPoint
import com.fitlogic.ai.core.domain.model.MuscleGroupDistributionPoint
import com.fitlogic.ai.core.domain.model.PrHistoryPoint
import com.fitlogic.ai.core.domain.model.WeeklyStatsSummary
import com.fitlogic.ai.core.domain.model.WeeklyVolumePoint
import com.fitlogic.ai.core.domain.model.WeightTrendPoint
import com.fitlogic.ai.core.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeeklyStatsSummaryUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(): Flow<WeeklyStatsSummary> = statsRepository.observeWeeklySummary()
    }

class GetExerciseProgressUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(exerciseCatalogId: String): Flow<List<ExerciseProgressPoint>> =
            statsRepository.observeExerciseProgress(exerciseCatalogId)
    }

class GetWeeklyVolumeUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(weekCount: Int = 8): Flow<List<WeeklyVolumePoint>> =
            statsRepository.observeWeeklyVolume(weekCount)
    }

class GetMuscleGroupDistributionUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(): Flow<List<MuscleGroupDistributionPoint>> =
            statsRepository.observeMuscleGroupDistribution()
    }

class GetPRHistoryUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(limit: Int = 30): Flow<List<PrHistoryPoint>> =
            statsRepository.observePrHistory(limit)
    }

class GetWeightTrendUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(limit: Int = 30): Flow<List<WeightTrendPoint>> =
            statsRepository.observeWeightTrend(limit)
    }

class AddWeightEntryUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        suspend operator fun invoke(
            weightKg: Float,
            measuredAt: Long = System.currentTimeMillis(),
        ): Result<Unit> = statsRepository.addWeightEntry(weightKg, measuredAt)
    }
