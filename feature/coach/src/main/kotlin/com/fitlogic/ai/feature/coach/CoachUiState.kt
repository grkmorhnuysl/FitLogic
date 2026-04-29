package com.fitlogic.ai.feature.coach

import com.fitlogic.ai.core.domain.model.AiInsight

data class CoachUiState(
    val isLoading: Boolean = false,
    val insights: List<AiInsight> = emptyList(),
    val selectedInsight: AiInsight? = null,
    val message: String? = null,
)
