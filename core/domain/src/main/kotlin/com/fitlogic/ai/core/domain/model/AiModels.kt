package com.fitlogic.ai.core.domain.model

import java.util.UUID

enum class AiInsightType {
    WEEKLY_REPORT,
    PLATEAU_ALERT,
    POST_WORKOUT,
    NUTRITION_ANALYSIS,
}

data class AiInsight(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: AiInsightType,
    val title: String,
    val body: String,
    val relatedWorkoutId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)
