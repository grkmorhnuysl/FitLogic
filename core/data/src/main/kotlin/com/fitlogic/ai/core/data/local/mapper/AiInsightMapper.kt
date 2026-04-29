package com.fitlogic.ai.core.data.local.mapper

import com.fitlogic.ai.core.data.local.entity.AiInsightEntity
import com.fitlogic.ai.core.domain.model.AiInsight
import com.fitlogic.ai.core.domain.model.AiInsightType

fun AiInsightEntity.toDomain(): AiInsight =
    AiInsight(
        id = id,
        userId = userId,
        type = AiInsightType.valueOf(type),
        title = title,
        body = body,
        relatedWorkoutId = relatedWorkoutId,
        createdAt = createdAt,
        readAt = readAt,
        updatedAt = updatedAt,
    )

fun AiInsight.toEntity(): AiInsightEntity =
    AiInsightEntity(
        id = id,
        userId = userId,
        type = type.name,
        title = title,
        body = body,
        relatedWorkoutId = relatedWorkoutId,
        createdAt = createdAt,
        readAt = readAt,
        updatedAt = updatedAt,
    )
