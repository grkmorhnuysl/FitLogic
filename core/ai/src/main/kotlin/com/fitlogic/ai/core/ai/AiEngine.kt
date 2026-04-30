package com.fitlogic.ai.core.ai

import kotlinx.coroutines.flow.Flow

interface AiEngine {
    val mode: AiEngineMode

    suspend fun generate(prompt: String): Result<String>

    fun generateStreaming(prompt: String): Flow<String>
}

enum class AiEngineMode {
    REMOTE_INFERENCE,
    ON_DEVICE_INFERENCE,
    DEMO_STUB,
}
