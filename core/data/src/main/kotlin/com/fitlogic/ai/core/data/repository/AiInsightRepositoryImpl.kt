package com.fitlogic.ai.core.data.repository

import com.fitlogic.ai.core.ai.AiEngine
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
        private val aiEngine: AiEngine,
    ) : AiInsightRepository {
        override fun observeActiveInsights(limit: Int): Flow<List<AiInsight>> =
            withUserId { userId ->
                aiInsightDao.observeByUserId(userId = userId, limit = limit).map { rows ->
                    rows.map {
                        it.toDomain().let { insight -> insight.copy(body = sanitizeLegacyInsightBody(insight.body)) }
                    }
                }
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
                val response = aiEngine.generate(prompt).getOrThrow()
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
                val response = aiEngine.generate(prompt).getOrThrow()
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
                val response = aiEngine.generate(prompt).getOrThrow()
                persistInsight(
                    userId = userId,
                    type = AiInsightType.POST_WORKOUT,
                    title = "Antrenman Sonrasi Yorum",
                    body = response,
                    relatedWorkoutId = workoutId,
                )
            }

        override suspend fun sendCoachMessage(message: String): Result<String> =
            runCatching {
                val normalizedMessage = message.trim()
                check(normalizedMessage.isNotEmpty()) { "Mesaj bos olamaz." }
                val prompt = promptBuilder.coachChat(message = normalizedMessage)
                val rawResponse = aiEngine.generate(prompt).getOrThrow()
                sanitizeCoachResponse(
                    response = rawResponse,
                    prompt = prompt,
                    userMessage = normalizedMessage,
                )
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { error -> Result.failure(IllegalStateException(mapCoachError(error))) },
            )

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

        private fun sanitizeCoachResponse(
            response: String,
            prompt: String,
            userMessage: String,
        ): String {
            val cleaned = response.trim()
            if (cleaned.isBlank()) error("AI_EMPTY_RESPONSE")
            val lower = cleaned.lowercase()
            val promptEcho =
                lower.contains("kullanici mesaji:") ||
                    lower.contains("veri:") ||
                    lower.contains(prompt.take(48).lowercase())
            if (promptEcho) error("AI_PROMPT_ECHO")

            val words = cleaned.split(Regex("\\s+")).filter { it.isNotBlank() }
            val uniqueRatio = words.toSet().size.toFloat() / words.size.coerceAtLeast(1)
            if (words.size >= 12 && uniqueRatio < 0.35f) error("AI_REPETITIVE_RESPONSE")

            val sameAsInput = cleaned.equals(userMessage.trim(), ignoreCase = true)
            if (sameAsInput) error("AI_LOW_QUALITY_RESPONSE")
            return cleaned
        }

        private fun mapCoachError(error: Throwable): String {
            val raw = error.message.orEmpty().lowercase()
            return when {
                raw.contains("openai_api_key") || raw.contains("servis anahtari eksik") ->
                    "AI servisi su an hazir degil. Lutfen birazdan tekrar dene."
                raw.contains("token") || raw.contains("auth") || raw.contains("401") || raw.contains("403") ->
                    "Oturumunda bir sorun var. Lutfen tekrar giris yapip yeniden dene."
                raw.contains("json") || raw.contains("parse") || raw.contains("serialization") ->
                    "Sunucu yaniti islenemedi. Kisa bir sure sonra tekrar dene."
                raw.contains("model") || raw.contains("llm") || raw.contains("mediapipe") ->
                    "Telefondaki AI modeli yuklenemedi. Model dosyasini kontrol edip tekrar dene."
                raw.contains("timeout") || raw.contains("network") || raw.contains("unable to resolve host") || raw.contains("ioexception") ->
                    "Baglanti sorunu yasandi. Internetini kontrol edip tekrar dene."
                raw.contains("ai_prompt_echo") || raw.contains("ai_empty_response") || raw.contains("ai_repetitive_response") || raw.contains("ai_low_quality_response") ->
                    "Su an net bir cevap uretemedim. Yeniden dene veya bugun icin hafif-orta tempoda 30 dakika antrenman yapmayi hedefle."
                else ->
                    "Mesajin su an gonderilemedi. Lutfen tekrar dene."
            }
        }

        private fun sanitizeLegacyInsightBody(raw: String): String {
            val lower = raw.lowercase()
            val looksLikePromptEcho =
                lower.contains("veri:") ||
                    lower.contains("kullanici mesaji:") ||
                    lower.contains("post-workout yorum")
            return if (looksLikePromptEcho) {
                "Bu icgorunun eski icerigi guncel degil. Yeni bir AI analizi olusturup daha net oneriler alabilirsin."
            } else {
                raw
            }
        }
    }
