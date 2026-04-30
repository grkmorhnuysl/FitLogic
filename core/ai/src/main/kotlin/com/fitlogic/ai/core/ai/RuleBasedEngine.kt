package com.fitlogic.ai.core.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleBasedEngine
    @Inject
    constructor() : AiEngine {
        override val mode: AiEngineMode = AiEngineMode.DEMO_STUB

        override suspend fun generate(prompt: String): Result<String> =
            runCatching {
                buildString {
                    append("Lite Mod Analizi:\n")
                    append("- Duzenli antrenman ritmini koru.\n")
                    append("- Her hafta 1 degiskeni kucuk adimla gelistir.\n")
                    append("- Uyku ve protein takibini ihmal etme.\n")
                    append("- Bugun icin tek odak: hareket kalitesi.")
                }
            }

        override fun generateStreaming(prompt: String): Flow<String> = flowOf("Lite Mod aktif. Kisa analiz hazir.")
    }
