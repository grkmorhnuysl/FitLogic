package com.fitlogic.ai.core.ai

import kotlinx.coroutines.flow.Flow

interface AiEngine {
    suspend fun generate(prompt: String): Result<String>

    fun generateStreaming(prompt: String): Flow<String>
}
