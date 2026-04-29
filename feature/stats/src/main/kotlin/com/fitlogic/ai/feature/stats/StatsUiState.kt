package com.fitlogic.ai.feature.stats

import com.fitlogic.ai.core.domain.model.ExerciseProgressPoint
import com.fitlogic.ai.core.domain.model.MuscleGroupDistributionPoint
import com.fitlogic.ai.core.domain.model.PrHistoryPoint
import com.fitlogic.ai.core.domain.model.WeeklyStatsSummary
import com.fitlogic.ai.core.domain.model.WeeklyVolumePoint
import com.fitlogic.ai.core.domain.model.WeightTrendPoint

data class StatsUiState(
    val isLoading: Boolean = true,
    val selectedExerciseId: String = "",
    val summary: WeeklyStatsSummary = WeeklyStatsSummary(0, 0f, 0, 0),
    val exerciseProgress: List<ExerciseProgressPoint> = emptyList(),
    val weeklyVolume: List<WeeklyVolumePoint> = emptyList(),
    val muscleDistribution: List<MuscleGroupDistributionPoint> = emptyList(),
    val prHistory: List<PrHistoryPoint> = emptyList(),
    val weightTrend: List<WeightTrendPoint> = emptyList(),
    val message: String? = null,
)
