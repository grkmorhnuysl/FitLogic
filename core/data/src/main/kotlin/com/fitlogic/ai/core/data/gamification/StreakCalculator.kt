package com.fitlogic.ai.core.data.gamification

import com.fitlogic.ai.core.domain.model.StreakInfo
import java.time.Instant
import java.time.ZoneId

class StreakCalculator(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun calculate(completedWorkoutTimestamps: List<Long>): StreakInfo {
        if (completedWorkoutTimestamps.isEmpty()) {
            return StreakInfo(currentDays = 0, longestDays = 0, lastWorkoutDayEpoch = null)
        }

        val dayEpochs =
            completedWorkoutTimestamps
                .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate().toEpochDay() }
                .distinct()
                .sorted()

        var longest = 1
        var currentRun = 1
        for (i in 1 until dayEpochs.size) {
            if (dayEpochs[i] == dayEpochs[i - 1] + 1) {
                currentRun++
                if (currentRun > longest) {
                    longest = currentRun
                }
            } else {
                currentRun = 1
            }
        }

        val today = Instant.now().atZone(zoneId).toLocalDate().toEpochDay()
        val latest = dayEpochs.last()
        var current = 0
        if (latest == today || latest == today - 1) {
            current = 1
            var cursor = latest
            for (index in dayEpochs.size - 2 downTo 0) {
                val day = dayEpochs[index]
                if (day == cursor - 1) {
                    current++
                    cursor = day
                } else {
                    break
                }
            }
        }

        return StreakInfo(currentDays = current, longestDays = longest, lastWorkoutDayEpoch = latest)
    }
}
