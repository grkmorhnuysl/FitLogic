package com.fitlogic.ai.feature.auth

enum class AuthMode {
    LOGIN,
    REGISTER,
    RESET_PASSWORD,
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val message: String? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isGoogleLoading: Boolean = false,
)
