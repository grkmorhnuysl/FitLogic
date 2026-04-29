package com.fitlogic.ai.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.usecase.user.ContinueAsGuestUseCase
import com.fitlogic.ai.core.domain.usecase.user.RegisterUseCase
import com.fitlogic.ai.core.domain.usecase.user.ResetPasswordUseCase
import com.fitlogic.ai.core.domain.usecase.user.SignInUseCase
import com.fitlogic.ai.core.domain.usecase.user.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val signInUseCase: SignInUseCase,
        private val registerUseCase: RegisterUseCase,
        private val resetPasswordUseCase: ResetPasswordUseCase,
        private val continueAsGuestUseCase: ContinueAsGuestUseCase,
        private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AuthUiState())
        val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

        fun onModeChange(mode: AuthMode) {
            _uiState.update { it.copy(mode = mode, message = null) }
        }

        fun onEmailChange(value: String) {
            _uiState.update { it.copy(email = value, message = null) }
        }

        fun onPasswordChange(value: String) {
            _uiState.update { it.copy(password = value, message = null) }
        }

        fun submit() {
            val state = _uiState.value
            if (!state.email.contains("@")) {
                _uiState.update { it.copy(message = "Geçerli bir email adresi girin.") }
                return
            }
            if (state.mode != AuthMode.RESET_PASSWORD && state.password.length < 6) {
                _uiState.update { it.copy(message = "Şifre en az 6 karakter olmalı.") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, message = null) }
                val result =
                    when (state.mode) {
                        AuthMode.LOGIN -> signInUseCase(state.email.trim(), state.password)
                        AuthMode.REGISTER -> registerUseCase(state.email.trim(), state.password)
                        AuthMode.RESET_PASSWORD -> resetPasswordUseCase(state.email.trim())
                    }
                _uiState.update {
                    if (result.isSuccess) {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = state.mode != AuthMode.RESET_PASSWORD,
                            message =
                                if (state.mode == AuthMode.RESET_PASSWORD) {
                                    "Şifre sıfırlama bağlantısı gönderildi."
                                } else {
                                    "Giriş başarılı."
                                },
                        )
                    } else {
                        it.copy(
                            isLoading = false,
                            message = result.exceptionOrNull()?.message ?: "İşlem başarısız. Tekrar deneyin.",
                        )
                    }
                }
            }
        }

        fun continueAsGuest() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, message = null) }
                val result = continueAsGuestUseCase()
                _uiState.update {
                    if (result.isSuccess) {
                        it.copy(isLoading = false, isAuthenticated = true, message = "Misafir moda geçildi.")
                    } else {
                        it.copy(isLoading = false, message = "Misafir mod başlatılamadı.")
                    }
                }
            }
        }

        fun signInWithGoogle(idToken: String) {
            if (idToken.isBlank()) {
                _uiState.update { it.copy(message = "Google girisi icin gecerli token alinmadi.") }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isGoogleLoading = true, message = null) }
                val result = signInWithGoogleUseCase(idToken)
                _uiState.update {
                    if (result.isSuccess) {
                        it.copy(
                            isGoogleLoading = false,
                            isAuthenticated = true,
                            message = "Google ile giris basarili.",
                        )
                    } else {
                        it.copy(
                            isGoogleLoading = false,
                            message = result.exceptionOrNull()?.message ?: "Google girisi basarisiz.",
                        )
                    }
                }
            }
        }

        fun onGoogleSignInError(message: String) {
            _uiState.update { it.copy(message = message) }
        }
    }
