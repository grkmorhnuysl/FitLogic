package com.fitlogic.ai.core.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAiRemoteEngine
    @Inject
    constructor() : AiEngine {
        override val mode: AiEngineMode = AiEngineMode.REMOTE_INFERENCE

        override suspend fun generate(prompt: String): Result<String> =
            runCatching {
                val apiKey = BuildConfig.OPENAI_API_KEY.trim()
                check(apiKey.isNotEmpty()) { "AI servis anahtari eksik. OPENAI_API_KEY tanimlanmali." }
                val baseUrl = BuildConfig.OPENAI_BASE_URL.trimEnd('/')
                val model = BuildConfig.OPENAI_MODEL.ifBlank { "gpt-4.1-mini" }
                val endpoint = "$baseUrl/chat/completions"

                val body =
                    json.encodeToString(
                        kotlinx.serialization.json.JsonObject.serializer(),
                        buildJsonObject {
                            put("model", model)
                            put(
                                "messages",
                                buildJsonArray {
                                    add(
                                        buildJsonObject {
                                            put("role", "user")
                                            put("content", prompt)
                                        },
                                    )
                                },
                            )
                            put("temperature", 0.7)
                        },
                    )

                withContext(Dispatchers.IO) {
                    val connection = (URL(endpoint).openConnection() as HttpURLConnection)
                    try {
                        connection.requestMethod = "POST"
                        connection.connectTimeout = 15_000
                        connection.readTimeout = 30_000
                        connection.doOutput = true
                        connection.setRequestProperty("Authorization", "Bearer $apiKey")
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.setRequestProperty("Accept", "application/json")
                        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                        val code = connection.responseCode
                        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                        val payload = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                        check(code in 200..299) { "AI servis hatasi ($code): $payload" }

                        val root = json.parseToJsonElement(payload).jsonObject
                        val content =
                            root["choices"]
                                ?.jsonArray
                                ?.firstOrNull()
                                ?.jsonObject
                                ?.get("message")
                                ?.jsonObject
                                ?.get("content")
                                ?.jsonPrimitive
                                ?.contentOrNull
                                ?.trim()
                        check(!content.isNullOrBlank()) { "AI bos cevap dondu." }
                        content
                    } finally {
                        connection.disconnect()
                    }
                }
            }

        override fun generateStreaming(prompt: String): Flow<String> =
            flow {
                val result = generate(prompt)
                result.onSuccess { emit(it) }.onFailure { throw it }
            }

        companion object {
            private val json = Json { ignoreUnknownKeys = true }
        }
    }
