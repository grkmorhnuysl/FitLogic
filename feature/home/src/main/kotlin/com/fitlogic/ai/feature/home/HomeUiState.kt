package com.fitlogic.ai.feature.home

import com.fitlogic.ai.core.domain.model.Achievement
import com.fitlogic.ai.core.domain.model.StreakInfo
import com.fitlogic.ai.core.domain.model.WeeklyGoal

data class HomeUiState(
    val isLoading: Boolean = true,
    val streak: StreakInfo = StreakInfo(0, 0, null),
    val weeklyGoal: WeeklyGoal = WeeklyGoal(targetWorkouts = 3, completedWorkouts = 0),
    val achievements: List<Achievement> = emptyList(),
    val latestUnlockedAchievement: Achievement? = null,
    val message: String? = null,
)
