package com.fitlogic.ai.core.data.remote

data class AuthUser(
    val id: String,
    val email: String,
)

enum class AuthProvider {
    EMAIL,
    GOOGLE,
}

data class AuthSession(
    val user: AuthUser,
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long?,
    val provider: AuthProvider,
)

data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long?,
)

interface AuthDataSource {
    suspend fun signIn(
        email: String,
        password: String,
    ): Result<AuthSession>

    suspend fun register(
        email: String,
        password: String,
    ): Result<AuthSession>

    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthSession>

    suspend fun resetPassword(email: String): Result<Unit>

    suspend fun refreshSession(refreshToken: String): Result<SessionTokens>

    suspend fun signOut(accessToken: String?): Result<Unit>

    suspend fun deleteCurrentAccount(accessToken: String?): Result<Unit>
}
