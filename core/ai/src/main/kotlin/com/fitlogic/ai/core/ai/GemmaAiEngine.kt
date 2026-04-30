package com.fitlogic.ai.core.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaAiEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : AiEngine {
        override val mode: AiEngineMode = AiEngineMode.ON_DEVICE_INFERENCE
        private val engineMutex = Mutex()
        private var llmInference: LlmInference? = null

        override suspend fun generate(prompt: String): Result<String> =
            runCatching {
                val modelResponse = generateOnDevice(prompt)
                modelResponse.ifBlank { CoachFallbackReplyBuilder.build(prompt) }
            }

        override fun generateStreaming(prompt: String): Flow<String> =
            flow {
                val tokens = generate(prompt).getOrElse { CoachFallbackReplyBuilder.build(prompt) }.split(" ")
                for (token in tokens) {
                    delay(35)
                    emit("$token ")
                }
            }

        private suspend fun generateOnDevice(prompt: String): String =
            withContext(Dispatchers.IO) {
                val modelPath = resolveModelPath()
                if (modelPath == null) {
                    return@withContext CoachFallbackReplyBuilder.build(prompt)
                }

                val engine = getOrCreateEngine(modelPath.absolutePath)
                engine.generateResponse(prompt).trim()
            }

        private fun resolveModelPath(): File? {
            val configuredPath = BuildConfig.LOCAL_LLM_MODEL_PATH.trim()
            val configuredFile = configuredPath.takeIf { it.isNotBlank() }?.let(::File)
            if (configuredFile?.exists() == true) return configuredFile

            return DEFAULT_MODEL_PATHS
                .map(::File)
                .firstOrNull { it.exists() }
        }

        private suspend fun getOrCreateEngine(modelPath: String): LlmInference =
            engineMutex.withLock {
                llmInference ?: createEngine(modelPath).also { llmInference = it }
            }

        private fun createEngine(modelPath: String): LlmInference {
            val options =
                LlmInference.LlmInferenceOptions
                    .builder()
                    .setModelPath(modelPath)
                    .setMaxTokens(BuildConfig.LOCAL_LLM_MAX_TOKENS)
                    .setMaxTopK(BuildConfig.LOCAL_LLM_MAX_TOP_K)
                    .build()
            return LlmInference.createFromOptions(context, options)
        }

        companion object {
            private val DEFAULT_MODEL_PATHS =
                listOf(
                    "/data/local/tmp/llm/gemma-2b-it.bin",
                    "/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin",
                    "/data/local/tmp/llm/model_version.task",
                )
        }
    }
