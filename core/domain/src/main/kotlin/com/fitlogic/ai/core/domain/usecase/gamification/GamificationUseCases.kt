package com.fitlogic.ai.core.domain.usecase.gamification

import com.fitlogic.ai.core.domain.model.NotificationSettings
import com.fitlogic.ai.core.domain.model.NotificationType
import com.fitlogic.ai.core.domain.model.StreakInfo
import com.fitlogic.ai.core.domain.model.WeeklyGoal
import com.fitlogic.ai.core.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveStreakUseCase
    @Inject
    constructor(
        private val repository: GamificationRepository,
    ) {
        operator fun invoke(): Flow<StreakInfo> = repository.observeStreak()
    }

class ObserveAchievementsUseCase
    @Inject
    constructor(
        private val repository: GamificationRepository,
    ) {
        operator fun invoke() = repository.observeAchievements()
    }

class CheckAchievementsUseCase
    @Inject
    constructor(
        private val repository: GamificationRepository,
    ) {
        suspend operator fun invoke() = repository.checkAchievements()
    }

class ObserveWeeklyGoalUseCase
    @Inject
    constructor(
        private val repository: GamificationRepository,
    ) {
        operator fun invoke(): Flow<WeeklyGoal> = repository.observeWeeklyGoal()
    }

class SetWeeklyGoalUseCase
    @Inject
    constructor(
        private val repository: GamificationRepository,
    ) {
        suspend operator fun invoke(targetWorkouts: Int) = repository.setWeeklyGoal(targetWorkouts)
    }

class ObserveNotificationSettingsUseCase
    @Inject
    constructor(
        private val repository: GamificationRepository,
    ) {
        operator fun invoke(): Flow<NotificationSettings> = repository.observeNotificationSettings()
    }

class SetNotificationEnabledUseCase
    @Inject
    constructor(
        private val repository: GamificationRepository,
    ) {
        suspend operator fun invoke(
            type: NotificationType,
            enabled: Boolean,
        ) = repository.setNotificationEnabled(type, enabled)
    }
