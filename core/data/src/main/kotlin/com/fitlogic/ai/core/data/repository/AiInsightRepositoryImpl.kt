package com.fitlogic.ai.core.data.repository

import com.fitlogic.ai.core.ai.AiEngine
import com.fitlogic.ai.core.ai.DeviceProfile
import com.fitlogic.ai.core.ai.GemmaAiEngine
import com.fitlogic.ai.core.ai.RuleBasedEngine
import com.fitlogic.ai.core.ai.prompt.PromptBuilder
import com.fitlogic.ai.core.data.local.dao.AiInsightDao
import com.fitlogic.ai.core.data.local.dao.SetDao
import com.fitlogic.ai.core.data.local.dao.WorkoutDao
import com.fitlogic.ai.core.data.local.entity.AiInsightEntity
import com.fitlogic.ai.core.data.local.mapper.toDomain
import com.fitlogic.ai.core.data.local.session.SessionPreferences
import com.fitlogic.ai.core.domain.model.AiInsight
import com.fitlogic.ai.core.domain.model.AiInsightType
import com.fitlogic.ai.core.domain.repository.AiInsightRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class AiInsightRepositoryImpl
    @Inject
    constructor(
        private val aiInsightDao: AiInsightDao,
        private val workoutDao: WorkoutDao,
        private val setDao: SetDao,
        private val sessionPreferences: SessionPreferences,
        private val promptBuilder: PromptBuilder,
        private val gemmaAiEngine: GemmaAiEngine,
        private val ruleBasedEngine: RuleBasedEngine,
        private val deviceProfile: DeviceProfile,
    ) : AiInsightRepository {
        override fun observeActiveInsights(limit: Int): Flow<List<AiInsight>> =
            withUserId { userId ->
                aiInsightDao.observeByUserId(userId = userId, limit = limit).map { rows -> rows.map { it.toDomain() } }
            }

        override fun observeInsightDetail(insightId: String): Flow<AiInsight?> =
            aiInsightDao.observeById(insightId).map { it?.toDomain() }

        override suspend fun markInsightAsRead(insightId: String): Result<Unit> =
            runCatching {
                val now = System.currentTimeMillis()
                aiInsightDao.markAsRead(insightId = insightId, readAt = now, updatedAt = now)
            }

        override suspend fun generateWeeklyReport(): Result<AiInsight> =
            runCatching {
                val userId = requireCurrentUserId()
                val summary = buildWeeklySummary(userId)
                val prompt = promptBuilder.weeklyReport(summary)
                val response = selectEngine().generate(prompt).getOrThrow()
                persistInsight(
                    userId = userId,
                    type = AiInsightType.WEEKLY_REPORT,
                    title = "Haftalik AI Raporu",
                    body = response,
                    relatedWorkoutId = null,
                )
            }

        override suspend fun detectPlateau(): Result<AiInsight?> =
            runCatching {
                val userId = requireCurrentUserId()
                val summary = buildWeeklySummary(userId)
                val prompt = promptBuilder.plateau(summary)
                val response = selectEngine().generate(prompt).getOrThrow()
                val shouldCreateInsight = response.contains("plato", ignoreCase = true)
                if (!shouldCreateInsight) {
                    null
                } else {
                    persistInsight(
                        userId = userId,
                        type = AiInsightType.PLATEAU_ALERT,
                        title = "Plato Uyarisi",
                        body = response,
                        relatedWorkoutId = null,
                    )
                }
            }

        override suspend fun getPostWorkoutInsight(workoutId: String): Result<AiInsight> =
            runCatching {
                val userId = requireCurrentUserId()
                val workout = requireNotNull(workoutDao.getById(workoutId)) { "Antrenman bulunamadi." }
                val prompt =
                    promptBuilder.postWorkout(
                        "Antrenman: ${workout.title}, hacim: ${workout.totalVolume}, durum: ${workout.status}",
                    )
                val response = selectEngine().generate(prompt).getOrThrow()
                persistInsight(
                    userId = userId,
                    type = AiInsightType.POST_WORKOUT,
                    title = "Antrenman Sonrasi Yorum",
                    body = response,
                    relatedWorkoutId = workoutId,
                )
            }

        private suspend fun persistInsight(
            userId: String,
            type: AiInsightType,
            title: String,
            body: String,
            relatedWorkoutId: String?,
        ): AiInsight {
            val now = System.currentTimeMillis()
            val insight =
                AiInsight(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = type,
                    title = title,
                    body = body,
                    relatedWorkoutId = relatedWorkoutId,
                    createdAt = now,
                    updatedAt = now,
                )
            aiInsightDao.insert(
                AiInsightEntity(
                    id = insight.id,
                    userId = insight.userId,
                    type = insight.type.name,
                    title = insight.title,
                    body = insight.body,
                    relatedWorkoutId = insight.relatedWorkoutId,
                    createdAt = insight.createdAt,
                    updatedAt = insight.updatedAt,
                ),
            )
            return insight
        }

        private suspend fun buildWeeklySummary(userId: String): String {
            val weekStart = System.currentTimeMillis() - WEEK_MILLIS
            val summary = workoutDao.observeWeeklySummary(userId = userId, fromEpochMs = weekStart).first()
            val prCount = setDao.observeWeeklyPrCount(userId = userId, fromEpochMs = weekStart).first()
            return "workouts=${summary.workoutsCompleted}, volume=${summary.totalVolume}, prCount=$prCount, avgMin=${summary.avgDurationMinutes}"
        }

        private fun selectEngine(): AiEngine = if (deviceProfile.isLiteMode()) ruleBasedEngine else gemmaAiEngine

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
