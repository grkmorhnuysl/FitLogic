package com.fitlogic.ai.core.domain.model

data class StreakInfo(
    val currentDays: Int,
    val longestDays: Int,
    val lastWorkoutDayEpoch: Long?,
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlockedAt: Long?,
    val progress: Int,
    val target: Int,
)

data class WeeklyGoal(
    val targetWorkouts: Int,
    val completedWorkouts: Int,
)

enum class NotificationType {
    WORKOUT_REMINDER,
    WATER_REMINDER,
    WEEKLY_REPORT,
    PR_CELEBRATION,
    STREAK_SAVE,
}

data class NotificationSettings(
    val workoutReminderEnabled: Boolean = true,
    val waterReminderEnabled: Boolean = true,
    val weeklyReportEnabled: Boolean = true,
    val prCelebrationEnabled: Boolean = true,
    val streakSaveEnabled: Boolean = true,
)
