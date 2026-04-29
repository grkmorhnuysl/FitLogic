package com.fitlogic.ai.core.ai

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceProfile
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun isLiteMode(): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalRamGb = memoryInfo.totalMem / ONE_GB
            return totalRamGb < 4L
        }

        companion object {
            private const val ONE_GB = 1024L * 1024L * 1024L
        }
    }
