package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "workout_reminder_enabled")
    val workoutReminderEnabled: Boolean = true,
    @ColumnInfo(name = "water_reminder_enabled")
    val waterReminderEnabled: Boolean = true,
    @ColumnInfo(name = "weekly_report_enabled")
    val weeklyReportEnabled: Boolean = true,
    @ColumnInfo(name = "pr_celebration_enabled")
    val prCelebrationEnabled: Boolean = true,
    @ColumnInfo(name = "streak_save_enabled")
    val streakSaveEnabled: Boolean = true,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
