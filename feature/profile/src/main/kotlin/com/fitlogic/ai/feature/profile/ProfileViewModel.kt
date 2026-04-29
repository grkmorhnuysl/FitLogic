package com.fitlogic.ai.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.model.LanguagePreference
import com.fitlogic.ai.core.domain.model.NotificationType
import com.fitlogic.ai.core.domain.model.ThemePreference
import com.fitlogic.ai.core.domain.model.WeightUnit
import com.fitlogic.ai.core.domain.usecase.gamification.ObserveNotificationSettingsUseCase
import com.fitlogic.ai.core.domain.usecase.gamification.SetNotificationEnabledUseCase
import com.fitlogic.ai.core.domain.usecase.sync.ObserveSyncStatusUseCase
import com.fitlogic.ai.core.domain.usecase.sync.TriggerSyncNowUseCase
import com.fitlogic.ai.core.domain.usecase.user.DeleteAccountUseCase
import com.fitlogic.ai.core.domain.usecase.user.ObserveCurrentProfileUseCase
import com.fitlogic.ai.core.domain.usecase.user.SignOutUseCase
import com.fitlogic.ai.core.domain.usecase.user.UpdateProfileUseCase
import com.fitlogic.ai.core.domain.usecase.user.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        observeCurrentProfileUseCase: ObserveCurrentProfileUseCase,
        private val updateProfileUseCase: UpdateProfileUseCase,
        private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val deleteAccountUseCase: DeleteAccountUseCase,
        observeNotificationSettingsUseCase: ObserveNotificationSettingsUseCase,
        private val setNotificationEnabledUseCase: SetNotificationEnabledUseCase,
        observeSyncStatusUseCase: ObserveSyncStatusUseCase,
        private val triggerSyncNowUseCase: TriggerSyncNowUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ProfileUiState())
        val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                observeCurrentProfileUseCase().collect { profile ->
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            displayNameInput = profile?.displayName.orEmpty(),
                            themePreference = profile?.themePreference ?: it.themePreference,
                            languagePreference = profile?.languagePreference ?: it.languagePreference,
                            weightUnit = profile?.unitPreferences?.weightUnit ?: it.weightUnit,
                            isLoading = false,
                        )
                    }
                }
            }
            viewModelScope.launch {
                observeNotificationSettingsUseCase().collect { settings ->
                    _uiState.update { it.copy(notificationSettings = settings) }
                }
            }
            viewModelScope.launch {
                observeSyncStatusUseCase().collect { status ->
                    _uiState.update { it.copy(syncStatus = status) }
                }
            }
        }

        fun onDisplayNameChange(value: String) {
            _uiState.update { it.copy(displayNameInput = value, message = null) }
        }

        fun onThemeChange(value: ThemePreference) {
            _uiState.update { it.copy(themePreference = value, message = null) }
        }

        fun onLanguageChange(value: LanguagePreference) {
            _uiState.update { it.copy(languagePreference = value, message = null) }
        }

        fun onWeightUnitChange(value: WeightUnit) {
            _uiState.update { it.copy(weightUnit = value, message = null) }
        }

        fun saveProfile() {
            val current = _uiState.value.profile ?: return
            val updated =
                current.copy(
                    displayName = _uiState.value.displayNameInput,
                    themePreference = _uiState.value.themePreference,
                    languagePreference = _uiState.value.languagePreference,
                    unitPreferences = current.unitPreferences.copy(weightUnit = _uiState.value.weightUnit),
                )
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, message = null) }
                val result = updateProfileUseCase(updated)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = if (result.isSuccess) "Profil kaydedildi." else "Profil kaydedilemedi.",
                    )
                }
            }
        }

        fun savePreferencesOnly() {
            val current = _uiState.value.profile ?: return
            val updated =
                current.copy(
                    themePreference = _uiState.value.themePreference,
                    languagePreference = _uiState.value.languagePreference,
                    unitPreferences = current.unitPreferences.copy(weightUnit = _uiState.value.weightUnit),
                )
            viewModelScope.launch {
                val result = updateUserPreferencesUseCase(updated)
                _uiState.update {
                    it.copy(message = if (result.isSuccess) "Ayarlar guncellendi." else "Ayarlar guncellenemedi.")
                }
            }
        }

        fun exportData() {
            _uiState.update { it.copy(message = "Veri disa aktarma tetiklendi (MVP: placeholder).") }
        }

        fun signOut() {
            viewModelScope.launch {
                signOutUseCase()
                _uiState.update { it.copy(message = "Cikis yapildi.") }
            }
        }

        fun requestDeleteAccount() {
            _uiState.update { it.copy(isDeleteDialogVisible = true, deleteConfirmInput = "", message = null) }
        }

        fun cancelDeleteAccount() {
            _uiState.update { it.copy(isDeleteDialogVisible = false, deleteConfirmInput = "") }
        }

        fun onDeleteConfirmInputChange(value: String) {
            _uiState.update { it.copy(deleteConfirmInput = value) }
        }

        fun confirmDeleteAccount() {
            if (_uiState.value.deleteConfirmInput != DELETE_CONFIRM_PHRASE) {
                _uiState.update { it.copy(message = "Silme onayi icin HESABIMI SIL yazin.") }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isDeleteInProgress = true, message = null) }
                val result = deleteAccountUseCase()
                _uiState.update {
                    it.copy(
                        isDeleteInProgress = false,
                        isDeleteDialogVisible = false,
                        deleteConfirmInput = "",
                        message = if (result.isSuccess) "Hesap silme islemi tamamlandi." else "Hesap silinemedi.",
                    )
                }
            }
        }

        fun setNotificationEnabled(
            type: NotificationType,
            enabled: Boolean,
        ) {
            viewModelScope.launch {
                setNotificationEnabledUseCase(type, enabled)
                    .onFailure {
                        _uiState.update { state -> state.copy(message = "Bildirim ayari guncellenemedi.") }
                    }
            }
        }

        fun syncNow() {
            viewModelScope.launch {
                val result = triggerSyncNowUseCase()
                _uiState.update {
                    it.copy(
                        message = if (result.isSuccess) "Senkronizasyon kuyruga alindi." else "Senkronizasyon baslatilamadi.",
                    )
                }
            }
        }

        companion object {
            const val DELETE_CONFIRM_PHRASE = "HESABIMI SIL"
        }
    }
