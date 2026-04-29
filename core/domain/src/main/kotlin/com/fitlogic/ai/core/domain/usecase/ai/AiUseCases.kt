package com.fitlogic.ai.core.domain.usecase.ai

import com.fitlogic.ai.core.domain.model.AiInsight
import com.fitlogic.ai.core.domain.repository.AiInsightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAiInsightsUseCase
    @Inject
    constructor(
        private val aiInsightRepository: AiInsightRepository,
    ) {
        operator fun invoke(limit: Int = 30): Flow<List<AiInsight>> = aiInsightRepository.observeActiveInsights(limit)
    }

class ObserveAiInsightDetailUseCase
    @Inject
    constructor(
        private val aiInsightRepository: AiInsightRepository,
    ) {
        operator fun invoke(insightId: String): Flow<AiInsight?> = aiInsightRepository.observeInsightDetail(insightId)
    }

class MarkAiInsightAsReadUseCase
    @Inject
    constructor(
        private val aiInsightRepository: AiInsightRepository,
    ) {
        suspend operator fun invoke(insightId: String): Result<Unit> = aiInsightRepository.markInsightAsRead(insightId)
    }

class GenerateWeeklyReportUseCase
    @Inject
    constructor(
        private val aiInsightRepository: AiInsightRepository,
    ) {
        suspend operator fun invoke(): Result<AiInsight> = aiInsightRepository.generateWeeklyReport()
    }

class DetectPlateauUseCase
    @Inject
    constructor(
        private val aiInsightRepository: AiInsightRepository,
    ) {
        suspend operator fun invoke(): Result<AiInsight?> = aiInsightRepository.detectPlateau()
    }

class GetPostWorkoutInsightUseCase
    @Inject
    constructor(
        private val aiInsightRepository: AiInsightRepository,
    ) {
        suspend operator fun invoke(workoutId: String): Result<AiInsight> = aiInsightRepository.getPostWorkoutInsight(workoutId)
    }
