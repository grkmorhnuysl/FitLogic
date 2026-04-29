package com.fitlogic.ai.core.data.remote

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseAuthDataSourceTest {
    @Test
    fun `sign in should return auth session for valid payload`() =
        runTest {
            val dataSource =
                SupabaseAuthDataSource(
                    config = sampleConfig(),
                    transport =
                        FakeSupabaseTransport(
                            responseByPath =
                                mapOf(
                                    "token?grant_type=password" to
                                        HttpResult(
                                            statusCode = 200,
                                            body =
                                                """
                                                {
                                                  "access_token": "access-1",
                                                  "refresh_token": "refresh-1",
                                                  "expires_in": 3600,
                                                  "user": {"id":"user-1", "email":"test@example.com"}
                                                }
                                                """.trimIndent(),
                                        ),
                                ),
                        ),
                )

            val result = dataSource.signIn("test@example.com", "123456")

            assertTrue(result.isSuccess)
            assertEquals("test@example.com", result.getOrThrow().user.email)
            assertEquals(AuthProvider.EMAIL, result.getOrThrow().provider)
        }

    @Test
    fun `google sign in should map provider and failure message`() =
        runTest {
            val dataSource =
                SupabaseAuthDataSource(
                    config = sampleConfig(),
                    transport =
                        FakeSupabaseTransport(
                            responseByPath =
                                mapOf(
                                    "token?grant_type=id_token" to
                                        HttpResult(
                                            statusCode = 400,
                                            body = """{"error":"invalid login credentials"}""",
                                        ),
                                ),
                        ),
                )

            val result = dataSource.signInWithGoogleIdToken("id-token")

            assertTrue(result.isFailure)
            assertEquals(AuthException.InvalidCredentials.message, result.exceptionOrNull()?.message)
        }

    @Test
    fun `refresh session should parse tokens`() =
        runTest {
            val dataSource =
                SupabaseAuthDataSource(
                    config = sampleConfig(),
                    transport =
                        FakeSupabaseTransport(
                            responseByPath =
                                mapOf(
                                    "token?grant_type=refresh_token" to
                                        HttpResult(
                                            statusCode = 200,
                                            body =
                                                """
                                                {
                                                  "access_token": "new-access",
                                                  "refresh_token": "new-refresh",
                                                  "expires_in": 1800
                                                }
                                                """.trimIndent(),
                                        ),
                                ),
                        ),
                )

            val result = dataSource.refreshSession("refresh-1")

            assertTrue(result.isSuccess)
            assertEquals("new-access", result.getOrThrow().accessToken)
            assertEquals("new-refresh", result.getOrThrow().refreshToken)
        }

    private fun sampleConfig(): SupabaseClientConfig =
        SupabaseClientConfig(
            url = "https://example.supabase.co",
            anonKey = "anon-key",
            projectId = "project-id",
        )
}

private class FakeSupabaseTransport(
    private val responseByPath: Map<String, HttpResult>,
) : SupabaseTransport {
    override fun request(
        config: SupabaseClientConfig,
        method: String,
        path: String,
        payload: kotlinx.serialization.json.JsonObject?,
        bearerToken: String?,
    ): HttpResult = responseByPath[path] ?: HttpResult(statusCode = 404, body = """{"error":"not found"}""")
}
