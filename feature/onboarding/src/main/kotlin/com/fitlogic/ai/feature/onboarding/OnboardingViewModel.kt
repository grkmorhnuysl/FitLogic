package com.fitlogic.ai.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.model.OnboardingDraft
import com.fitlogic.ai.core.domain.usecase.user.CalculateMacroTargetsUseCase
import com.fitlogic.ai.core.domain.usecase.user.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val calculateMacroTargetsUseCase: CalculateMacroTargetsUseCase,
        private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(OnboardingUiState())
        val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

        fun onDisplayNameChange(value: String) {
            _uiState.update { it.copy(displayName = value, message = null) }
        }

        fun onAgeChange(value: String) {
            _uiState.update { it.copy(age = value.filter(Char::isDigit), message = null) }
        }

        fun onHeightChange(value: String) {
            _uiState.update { it.copy(heightCm = value.filter { ch -> ch.isDigit() || ch == '.' }, message = null) }
        }

        fun onWeightChange(value: String) {
            _uiState.update { it.copy(weightKg = value.filter { ch -> ch.isDigit() || ch == '.' }, message = null) }
        }

        fun onGenderChange(value: com.fitlogic.ai.core.domain.model.Gender) {
            _uiState.update { it.copy(gender = value, message = null) }
        }

        fun onActivityLevelChange(value: com.fitlogic.ai.core.domain.model.ActivityLevel) {
            _uiState.update { it.copy(activityLevel = value, message = null) }
        }

        fun onGoalTypeChange(value: com.fitlogic.ai.core.domain.model.GoalType) {
            _uiState.update { it.copy(goalType = value, message = null) }
        }

        fun nextStep() {
            val state = _uiState.value
            when (state.step) {
                OnboardingStep.WELCOME -> _uiState.update { it.copy(step = OnboardingStep.BASIC_INFO) }
                OnboardingStep.BASIC_INFO -> {
                    val hasMissingBasicFields =
                        state.displayName.isBlank() ||
                            state.age.toIntOrNull() == null ||
                            state.heightCm.toFloatOrNull() == null ||
                            state.weightKg.toFloatOrNull() == null
                    if (hasMissingBasicFields) {
                        _uiState.update { it.copy(message = "Temel bilgileri eksiksiz doldurun.") }
                        return
                    }
                    _uiState.update { it.copy(step = OnboardingStep.GOAL) }
                }
                OnboardingStep.GOAL -> {
                    val suggestion =
                        calculateMacroTargetsUseCase(
                            gender = state.gender,
                            age = state.age.toInt(),
                            heightCm = state.heightCm.toFloat(),
                            weightKg = state.weightKg.toFloat(),
                            activityLevel = state.activityLevel,
                            goalType = state.goalType,
                        )
                    _uiState.update { it.copy(step = OnboardingStep.SUMMARY, suggestion = suggestion) }
                }
                OnboardingStep.SUMMARY -> Unit
            }
        }

        fun previousStep() {
            val previous =
                when (_uiState.value.step) {
                    OnboardingStep.WELCOME -> OnboardingStep.WELCOME
                    OnboardingStep.BASIC_INFO -> OnboardingStep.WELCOME
                    OnboardingStep.GOAL -> OnboardingStep.BASIC_INFO
                    OnboardingStep.SUMMARY -> OnboardingStep.GOAL
                }
            _uiState.update { it.copy(step = previous, message = null) }
        }

        fun complete() {
            val state = _uiState.value
            if (state.step != OnboardingStep.SUMMARY) return
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, message = null) }
                val result =
                    completeOnboardingUseCase(
                        OnboardingDraft(
                            displayName = state.displayName.trim(),
                            age = state.age.toIntOrNull(),
                            heightCm = state.heightCm.toFloatOrNull(),
                            weightKg = state.weightKg.toFloatOrNull(),
                            gender = state.gender,
                            activityLevel = state.activityLevel,
                            goalType = state.goalType,
                        ),
                    )
                _uiState.update {
                    if (result.isSuccess) {
                        it.copy(isLoading = false, isCompleted = true)
                    } else {
                        it.copy(
                            isLoading = false,
                            message = result.exceptionOrNull()?.message ?: "Onboarding tamamlanamadı.",
                        )
                    }
                }
            }
        }
    }
