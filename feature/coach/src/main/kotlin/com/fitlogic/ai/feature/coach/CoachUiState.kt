package com.fitlogic.ai.feature.coach

import com.fitlogic.ai.core.domain.model.AiInsight

data class CoachUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val insights: List<AiInsight> = emptyList(),
    val selectedInsight: AiInsight? = null,
    val chatMessages: List<CoachChatMessage> = emptyList(),
    val draftMessage: String = "",
    val chatError: String? = null,
    val message: String? = null,
)

data class CoachChatMessage(
    val role: CoachMessageRole,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class CoachMessageRole {
    USER,
    ASSISTANT,
}
