package com.fitlogic.ai.core.domain.repository

import com.fitlogic.ai.core.domain.model.Achievement
import com.fitlogic.ai.core.domain.model.NotificationSettings
import com.fitlogic.ai.core.domain.model.NotificationType
import com.fitlogic.ai.core.domain.model.StreakInfo
import com.fitlogic.ai.core.domain.model.WeeklyGoal
import kotlinx.coroutines.flow.Flow

interface GamificationRepository {
    fun observeStreak(): Flow<StreakInfo>

    fun observeAchievements(): Flow<List<Achievement>>

    fun observeWeeklyGoal(): Flow<WeeklyGoal>

    fun observeNotificationSettings(): Flow<NotificationSettings>

    suspend fun setWeeklyGoal(targetWorkouts: Int): Result<Unit>

    suspend fun checkAchievements(): Result<List<Achievement>>

    suspend fun setNotificationEnabled(
        type: NotificationType,
        enabled: Boolean,
    ): Result<Unit>
}
