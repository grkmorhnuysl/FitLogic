package com.fitlogic.ai.core.domain.repository

import com.fitlogic.ai.core.domain.model.AiInsight
import kotlinx.coroutines.flow.Flow

interface AiInsightRepository {
    fun observeActiveInsights(limit: Int = 30): Flow<List<AiInsight>>

    fun observeInsightDetail(insightId: String): Flow<AiInsight?>

    suspend fun markInsightAsRead(insightId: String): Result<Unit>

    suspend fun generateWeeklyReport(): Result<AiInsight>

    suspend fun detectPlateau(): Result<AiInsight?>

    suspend fun getPostWorkoutInsight(workoutId: String): Result<AiInsight>

    suspend fun sendCoachMessage(message: String): Result<String>
}
