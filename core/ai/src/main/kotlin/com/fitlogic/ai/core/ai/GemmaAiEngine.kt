package com.fitlogic.ai.core.ai

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaAiEngine
    @Inject
    constructor() : AiEngine {
        override suspend fun generate(prompt: String): Result<String> =
            runCatching {
                delay(350)
                "[Gemma] $prompt\n\nOdak: surekli ilerleme, guvenli teknik ve toparlanma."
            }

        override fun generateStreaming(prompt: String): Flow<String> =
            flow {
                val tokens =
                    listOf(
                        "Antrenmanin iyi gidiyor.",
                        " Son hafta hacim trendin pozitif.",
                        " Formu bozmadan agirlik artisini surdur.",
                    )
                for (token in tokens) {
                    delay(120)
                    emit(token)
                }
            }
    }
