@file:Suppress("MaxLineLength")

package com.fitlogic.ai.core.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class SupabaseClientConfig(
    val url: String,
    val anonKey: String,
    val projectId: String,
)

internal data class HttpResult(
    val statusCode: Int,
    val body: String,
)

internal interface SupabaseTransport {
    fun request(
        config: SupabaseClientConfig,
        method: String,
        path: String,
        payload: JsonObject?,
        bearerToken: String?,
    ): HttpResult
}

private class DefaultSupabaseTransport : SupabaseTransport {
    private val json = Json { ignoreUnknownKeys = true }

    override fun request(
        config: SupabaseClientConfig,
        method: String,
        path: String,
        payload: JsonObject?,
        bearerToken: String?,
    ): HttpResult {
        val endpoint = "${config.url.trimEnd('/')}/auth/v1/$path"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection)
        return try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("apikey", config.anonKey)
            connection.setRequestProperty("x-client-info", "fitlogic-android")
            connection.setRequestProperty("Accept", "application/json")
            if (payload != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                val body = json.encodeToString(JsonObject.serializer(), payload)
                connection.outputStream.use { output -> output.write(body.toByteArray(Charsets.UTF_8)) }
            }
            if (!bearerToken.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            HttpResult(statusCode = status, body = body)
        } catch (ioException: IOException) {
            throw AuthException.Network(ioException)
        } finally {
            connection.disconnect()
        }
    }
}

@Singleton
@Suppress("TooManyFunctions")
class SupabaseAuthDataSource
    @Inject
    constructor(
        private val config: SupabaseClientConfig,
    ) : AuthDataSource {
        private val json = Json { ignoreUnknownKeys = true }
        private val transport: SupabaseTransport = DefaultSupabaseTransport()

        internal constructor(
            config: SupabaseClientConfig,
            transport: SupabaseTransport,
        ) : this(config) {
            this.overrideTransport = transport
        }

        private var overrideTransport: SupabaseTransport? = null

        private val activeTransport: SupabaseTransport
            get() = overrideTransport ?: transport

        override suspend fun signIn(
            email: String,
            password: String,
        ): Result<AuthSession> =
            requestAuthSession("token?grant_type=password", AuthProvider.EMAIL) {
                put("email", email)
                put("password", password)
            }

        override suspend fun register(
            email: String,
            password: String,
        ): Result<AuthSession> =
            requestAuthSession("signup", AuthProvider.EMAIL) {
                put("email", email)
                put("password", password)
            }

        override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthSession> =
            requestAuthSession("token?grant_type=id_token", AuthProvider.GOOGLE) {
                put("provider", "google")
                put("id_token", idToken)
            }

        override suspend fun resetPassword(email: String): Result<Unit> =
            runCatching {
                ensureRuntimeConfig()
                val result = post(path = "recover", payload = buildJsonObject { put("email", email) }, bearerToken = null)
                if (result.statusCode !in 200..299) {
                    throw mapAuthError(result.statusCode, result.body)
                }
            }

        override suspend fun refreshSession(refreshToken: String): Result<SessionTokens> =
            runCatching {
                ensureRuntimeConfig()
                val result =
                    post(
                        path = "token?grant_type=refresh_token",
                        payload = buildJsonObject { put("refresh_token", refreshToken) },
                        bearerToken = null,
                    )
                if (result.statusCode !in 200..299) {
                    throw mapAuthError(result.statusCode, result.body)
                }
                val payload = parseJsonObject(result.body)
                SessionTokens(
                    accessToken = payload.string("access_token"),
                    refreshToken = payload.string("refresh_token"),
                    expiresAtEpochSeconds = payload.expiresAtEpochSeconds(),
                )
            }

        override suspend fun signOut(accessToken: String?): Result<Unit> =
            runCatching {
                ensureRuntimeConfig()
                if (accessToken.isNullOrBlank()) return@runCatching
                val result = post(path = "logout", payload = null, bearerToken = accessToken)
                if (result.statusCode !in 200..299 && result.statusCode != 401) {
                    throw mapAuthError(result.statusCode, result.body)
                }
            }

        override suspend fun deleteCurrentAccount(accessToken: String?): Result<Unit> =
            runCatching {
                ensureRuntimeConfig()
                if (accessToken.isNullOrBlank()) {
                    throw AuthException.Unknown("Aktif oturum bulunamadi.")
                }
                val result = request(method = "DELETE", path = "user", payload = null, bearerToken = accessToken)
                if (result.statusCode !in 200..299 && result.statusCode != 401) {
                    throw mapAuthError(result.statusCode, result.body)
                }
            }

        private fun requestAuthSession(
            endpoint: String,
            provider: AuthProvider,
            payloadBuilder: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit,
        ): Result<AuthSession> =
            runCatching {
                ensureRuntimeConfig()
                val result = post(path = endpoint, payload = buildJsonObject(payloadBuilder), bearerToken = null)
                if (result.statusCode !in 200..299) {
                    throw mapAuthError(result.statusCode, result.body)
                }
                parseAuthSession(result.body, provider)
            }

        private fun parseAuthSession(
            responseBody: String,
            provider: AuthProvider,
        ): AuthSession {
            val payload = parseJsonObject(responseBody)
            val userObject = payload.objectOrNull("user") ?: payload
            return AuthSession(
                user =
                    AuthUser(
                        id = userObject.string("id"),
                        email = userObject.string("email"),
                    ),
                accessToken = payload.string("access_token"),
                refreshToken = payload.string("refresh_token"),
                expiresAtEpochSeconds = payload.expiresAtEpochSeconds(),
                provider = provider,
            )
        }

        private fun ensureRuntimeConfig() {
            if (config.url.isBlank() || config.anonKey.isBlank() || config.projectId.isBlank()) {
                throw AuthException.Unknown("Supabase ayarlari eksik. URL, anon key ve project id gerekli.")
            }
        }

        private fun post(
            path: String,
            payload: JsonObject?,
            bearerToken: String?,
        ): HttpResult = request("POST", path, payload, bearerToken)

        private fun request(
            method: String,
            path: String,
            payload: JsonObject?,
            bearerToken: String?,
        ): HttpResult = activeTransport.request(config, method, path, payload, bearerToken)

        private fun parseJsonObject(body: String): JsonObject =
            if (body.isBlank()) {
                JsonObject(emptyMap())
            } else {
                runCatching { json.parseToJsonElement(body).jsonObject }.getOrElse { JsonObject(emptyMap()) }
            }

        private fun mapAuthError(
            statusCode: Int,
            body: String,
        ): AuthException {
            val payload = parseJsonObject(body)
            val message =
                payload.stringOrNull("msg")
                    ?: payload.stringOrNull("message")
                    ?: payload.stringOrNull("error_description")
                    ?: payload.stringOrNull("error")
                    ?: "Giris islemi su an tamamlanamadi."
            val normalized = message.lowercase()
            return when {
                statusCode == 400 && normalized.contains("invalid login credentials") -> AuthException.InvalidCredentials
                statusCode == 400 && normalized.contains("email not confirmed") -> AuthException.InvalidCredentials
                statusCode == 422 && normalized.contains("already") -> AuthException.UserAlreadyExists
                statusCode == 401 -> AuthException.InvalidCredentials
                else -> AuthException.Unknown(message)
            }
        }

        private fun JsonObject.string(key: String): String =
            stringOrNull(key) ?: throw AuthException.Unknown("Beklenen alan bulunamadi: $key")

        private fun JsonObject.stringOrNull(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

        private fun JsonObject.objectOrNull(key: String): JsonObject? = this[key] as? JsonObject

        @Suppress("ReturnCount")
        private fun JsonObject.expiresAtEpochSeconds(): Long? {
            val expiresAt = stringOrNull("expires_at")?.toLongOrNull()
            if (expiresAt != null) return expiresAt
            val expiresIn = stringOrNull("expires_in")?.toLongOrNull() ?: return null
            return (System.currentTimeMillis() / 1000L) + expiresIn
        }
    }

sealed class AuthException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {
    data object InvalidCredentials : AuthException("Email veya sifre gecersiz.")

    data object UserAlreadyExists : AuthException("Bu email zaten kayitli.")

    class Network(cause: Throwable) : AuthException("Ag hatasi olustu. Baglantinizi kontrol edin.", cause)

    class Unknown(message: String) : AuthException(message)
}
