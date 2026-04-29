package com.fitlogic.ai.core.data.repository

import com.fitlogic.ai.core.data.gamification.StreakCalculator
import com.fitlogic.ai.core.data.local.dao.AchievementDao
import com.fitlogic.ai.core.data.local.dao.NotificationSettingsDao
import com.fitlogic.ai.core.data.local.dao.WeeklyGoalDao
import com.fitlogic.ai.core.data.local.dao.WorkoutDao
import com.fitlogic.ai.core.data.local.entity.AchievementEntity
import com.fitlogic.ai.core.data.local.entity.NotificationSettingsEntity
import com.fitlogic.ai.core.data.local.entity.WeeklyGoalEntity
import com.fitlogic.ai.core.data.local.session.SessionPreferences
import com.fitlogic.ai.core.domain.model.Achievement
import com.fitlogic.ai.core.domain.model.NotificationSettings
import com.fitlogic.ai.core.domain.model.NotificationType
import com.fitlogic.ai.core.domain.model.WeeklyGoal
import com.fitlogic.ai.core.domain.repository.GamificationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

@Singleton
class GamificationRepositoryImpl
    @Inject
    constructor(
        private val sessionPreferences: SessionPreferences,
        private val workoutDao: WorkoutDao,
        private val achievementDao: AchievementDao,
        private val weeklyGoalDao: WeeklyGoalDao,
        private val notificationSettingsDao: NotificationSettingsDao,
    ) : GamificationRepository {
        private val streakCalculator = StreakCalculator()

        override fun observeStreak() =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf(emptyList<Long>())
                workoutDao.observeFinishedWorkoutTimestamps(userId)
            }.map(streakCalculator::calculate)

        override fun observeAchievements(): Flow<List<Achievement>> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf(emptyList())
                achievementDao.observeByUserId(userId)
            }.map { entities -> entities.map(AchievementEntity::toDomain) }

        override fun observeWeeklyGoal(): Flow<WeeklyGoal> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf(WeeklyGoal(targetWorkouts = 3, completedWorkouts = 0))
                val startOfWeekEpoch = startOfWeekEpoch()
                combine(
                    weeklyGoalDao.observeByUserId(userId),
                    workoutDao.observeFinishedWorkoutCountSince(userId, startOfWeekEpoch),
                ) { goalEntity, completedCount ->
                    WeeklyGoal(
                        targetWorkouts = goalEntity?.targetWorkouts ?: 3,
                        completedWorkouts = completedCount,
                    )
                }
            }

        override fun observeNotificationSettings(): Flow<NotificationSettings> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf<NotificationSettingsEntity?>(null)
                notificationSettingsDao.observeByUserId(userId)
            }.map { entity ->
                if (entity == null) NotificationSettings() else entity.toDomain()
            }

        override suspend fun setWeeklyGoal(targetWorkouts: Int): Result<Unit> =
            runCatching {
                require(targetWorkouts in 1..14) { "Haftalik hedef 1 ile 14 arasinda olmali." }
                val userId = requireCurrentUserId()
                weeklyGoalDao.upsert(
                    WeeklyGoalEntity(
                        userId = userId,
                        targetWorkouts = targetWorkouts,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }

        override suspend fun checkAchievements(): Result<List<Achievement>> =
            runCatching {
                val userId = requireCurrentUserId()
                val finishedTimestamps = workoutDao.observeFinishedWorkoutTimestamps(userId).first()
                val streak = streakCalculator.calculate(finishedTimestamps)
                val finishedCount = finishedTimestamps.size

                val now = System.currentTimeMillis()
                val definitions = achievementDefinitions(finishedCount = finishedCount, streakDays = streak.currentDays)
                val updatedEntities = definitions.map { def ->
                    AchievementEntity(
                        id = def.id,
                        userId = userId,
                        title = def.title,
                        description = def.description,
                        progress = def.progress.coerceAtMost(def.target),
                        target = def.target,
                        unlockedAt = if (def.progress >= def.target) now else null,
                        updatedAt = now,
                    )
                }
                achievementDao.upsertAll(updatedEntities)
                updatedEntities.map(AchievementEntity::toDomain)
            }

        override suspend fun setNotificationEnabled(
            type: NotificationType,
            enabled: Boolean,
        ): Result<Unit> =
            runCatching {
                val userId = requireCurrentUserId()
                val current = notificationSettingsDao.observeByUserId(userId).first() ?: NotificationSettingsEntity(userId = userId)
                val next =
                    when (type) {
                        NotificationType.WORKOUT_REMINDER -> current.copy(workoutReminderEnabled = enabled)
                        NotificationType.WATER_REMINDER -> current.copy(waterReminderEnabled = enabled)
                        NotificationType.WEEKLY_REPORT -> current.copy(weeklyReportEnabled = enabled)
                        NotificationType.PR_CELEBRATION -> current.copy(prCelebrationEnabled = enabled)
                        NotificationType.STREAK_SAVE -> current.copy(streakSaveEnabled = enabled)
                    }.copy(updatedAt = System.currentTimeMillis())
                notificationSettingsDao.upsert(next)
            }

        private suspend fun requireCurrentUserId(): String {
            val snapshot = sessionPreferences.sessionSnapshot.first()
            return checkNotNull(snapshot.currentUserId) {
                "Aktif kullanici bulunamadi."
            }
        }

        private fun startOfWeekEpoch(): Long {
            val now = ZonedDateTime.now(ZoneId.systemDefault())
            val monday = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(now.zone)
            return monday.toInstant().toEpochMilli()
        }

        private fun achievementDefinitions(
            finishedCount: Int,
            streakDays: Int,
        ): List<AchievementDefinition> {
            val workoutTargets = listOf(1, 5, 10, 25, 50, 100)
            val streakTargets = listOf(3, 7, 14, 30)
            val results = mutableListOf<AchievementDefinition>()
            workoutTargets.forEach { target ->
                results +=
                    AchievementDefinition(
                        id = "workout_$target",
                        title = "$target antrenman",
                        description = "$target tamamlanmis antrenmana ulas.",
                        progress = finishedCount,
                        target = target,
                    )
            }
            streakTargets.forEach { target ->
                results +=
                    AchievementDefinition(
                        id = "streak_$target",
                        title = "$target gun seri",
                        description = "$target gun ust uste antrenman serisi yakala.",
                        progress = streakDays,
                        target = target,
                    )
            }
            while (results.size < 20) {
                val index = results.size + 1
                results +=
                    AchievementDefinition(
                        id = "milestone_$index",
                        title = "Kilometre tasi $index",
                        description = "Toplam antrenmanini artir.",
                        progress = finishedCount,
                        target = index * 15,
                    )
            }
            return results.take(20)
        }

        private data class AchievementDefinition(
            val id: String,
            val title: String,
            val description: String,
            val progress: Int,
            val target: Int,
        )
    }

private fun AchievementEntity.toDomain() =
    Achievement(
        id = id,
        title = title,
        description = description,
        unlockedAt = unlockedAt,
        progress = progress,
        target = target,
    )

private fun NotificationSettingsEntity.toDomain() =
    NotificationSettings(
        workoutReminderEnabled = workoutReminderEnabled,
        waterReminderEnabled = waterReminderEnabled,
        weeklyReportEnabled = weeklyReportEnabled,
        prCelebrationEnabled = prCelebrationEnabled,
        streakSaveEnabled = streakSaveEnabled,
    )
