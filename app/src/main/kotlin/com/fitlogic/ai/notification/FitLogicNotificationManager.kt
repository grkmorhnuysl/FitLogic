package com.fitlogic.ai.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FitLogicNotificationManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun ensureChannels() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            CHANNELS.forEach { channel ->
                manager.createNotificationChannel(channel)
            }
        }

        fun showSimpleNotification(
            channelId: String,
            title: String,
            body: String,
        ) {
            val notification =
                NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        }

        companion object {
            const val CHANNEL_WORKOUT = "workout_reminders"
            const val CHANNEL_WATER = "water_reminders"
            const val CHANNEL_WEEKLY = "weekly_report"
            const val CHANNEL_PR = "pr_celebration"
            const val CHANNEL_STREAK = "streak_save"

            private val CHANNELS =
                listOf(
                    NotificationChannel(CHANNEL_WORKOUT, "Antrenman Hatirlaticisi", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_WATER, "Su Hatirlaticisi", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_WEEKLY, "Haftalik Rapor", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_PR, "PR Kutlamasi", NotificationManager.IMPORTANCE_HIGH),
                    NotificationChannel(CHANNEL_STREAK, "Streak Koruma", NotificationManager.IMPORTANCE_HIGH),
                )
        }
    }
