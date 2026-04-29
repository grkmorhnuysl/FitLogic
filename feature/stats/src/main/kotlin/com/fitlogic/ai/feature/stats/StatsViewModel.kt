package com.fitlogic.ai.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.usecase.stats.AddWeightEntryUseCase
import com.fitlogic.ai.core.domain.usecase.stats.GetExerciseProgressUseCase
import com.fitlogic.ai.core.domain.usecase.stats.GetMuscleGroupDistributionUseCase
import com.fitlogic.ai.core.domain.usecase.stats.GetPRHistoryUseCase
import com.fitlogic.ai.core.domain.usecase.stats.GetWeeklyStatsSummaryUseCase
import com.fitlogic.ai.core.domain.usecase.stats.GetWeeklyVolumeUseCase
import com.fitlogic.ai.core.domain.usecase.stats.GetWeightTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        getWeeklyStatsSummaryUseCase: GetWeeklyStatsSummaryUseCase,
        private val getExerciseProgressUseCase: GetExerciseProgressUseCase,
        getWeeklyVolumeUseCase: GetWeeklyVolumeUseCase,
        getMuscleGroupDistributionUseCase: GetMuscleGroupDistributionUseCase,
        getPRHistoryUseCase: GetPRHistoryUseCase,
        getWeightTrendUseCase: GetWeightTrendUseCase,
        private val addWeightEntryUseCase: AddWeightEntryUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(StatsUiState())
        val uiState = _uiState.asStateFlow()

        init {
            observeBaseStreams(
                getWeeklyStatsSummaryUseCase = getWeeklyStatsSummaryUseCase,
                getWeeklyVolumeUseCase = getWeeklyVolumeUseCase,
                getMuscleGroupDistributionUseCase = getMuscleGroupDistributionUseCase,
                getPRHistoryUseCase = getPRHistoryUseCase,
                getWeightTrendUseCase = getWeightTrendUseCase,
            )
        }

        fun onExerciseIdChanged(exerciseId: String) {
            _uiState.update { it.copy(selectedExerciseId = exerciseId.trim()) }
            observeExerciseProgress()
        }

        fun addWeight(weightInput: String) {
            val weight = weightInput.replace(',', '.').toFloatOrNull()
            if (weight == null || weight <= 0f) {
                _uiState.update { it.copy(message = "Gecerli bir kilo degeri girin.") }
                return
            }
            viewModelScope.launch {
                addWeightEntryUseCase(weight).onFailure {
                    _uiState.update { state -> state.copy(message = it.message ?: "Kilo kaydi eklenemedi.") }
                }
            }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }

        private fun observeBaseStreams(
            getWeeklyStatsSummaryUseCase: GetWeeklyStatsSummaryUseCase,
            getWeeklyVolumeUseCase: GetWeeklyVolumeUseCase,
            getMuscleGroupDistributionUseCase: GetMuscleGroupDistributionUseCase,
            getPRHistoryUseCase: GetPRHistoryUseCase,
            getWeightTrendUseCase: GetWeightTrendUseCase,
        ) {
            viewModelScope.launch {
                combine(
                    getWeeklyStatsSummaryUseCase(),
                    getWeeklyVolumeUseCase(),
                    getMuscleGroupDistributionUseCase(),
                    getPRHistoryUseCase(),
                    getWeightTrendUseCase(),
                ) { summary, weeklyVolume, muscleDistribution, prHistory, weightTrend ->
                    StatsUiState(
                        isLoading = false,
                        selectedExerciseId = _uiState.value.selectedExerciseId,
                        summary = summary,
                        exerciseProgress = _uiState.value.exerciseProgress,
                        weeklyVolume = weeklyVolume,
                        muscleDistribution = muscleDistribution,
                        prHistory = prHistory,
                        weightTrend = weightTrend,
                        message = _uiState.value.message,
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }

        private fun observeExerciseProgress() {
            val exerciseId = _uiState.value.selectedExerciseId
            if (exerciseId.isBlank()) {
                _uiState.update { it.copy(exerciseProgress = emptyList()) }
                return
            }
            viewModelScope.launch {
                getExerciseProgressUseCase(exerciseId).collect { points ->
                    _uiState.update { it.copy(exerciseProgress = points) }
                }
            }
        }
    }
