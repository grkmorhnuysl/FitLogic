package com.fitlogic.ai.feature.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.usecase.ai.DetectPlateauUseCase
import com.fitlogic.ai.core.domain.usecase.ai.GenerateWeeklyReportUseCase
import com.fitlogic.ai.core.domain.usecase.ai.MarkAiInsightAsReadUseCase
import com.fitlogic.ai.core.domain.usecase.ai.ObserveAiInsightDetailUseCase
import com.fitlogic.ai.core.domain.usecase.ai.ObserveAiInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CoachViewModel
    @Inject
    constructor(
        observeAiInsightsUseCase: ObserveAiInsightsUseCase,
        private val observeAiInsightDetailUseCase: ObserveAiInsightDetailUseCase,
        private val generateWeeklyReportUseCase: GenerateWeeklyReportUseCase,
        private val detectPlateauUseCase: DetectPlateauUseCase,
        private val markAiInsightAsReadUseCase: MarkAiInsightAsReadUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(CoachUiState())
        val uiState = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                observeAiInsightsUseCase().collect { insights ->
                    _uiState.update { it.copy(insights = insights) }
                }
            }
        }

        fun selectInsight(insightId: String) {
            viewModelScope.launch {
                observeAiInsightDetailUseCase(insightId).collect { insight ->
                    _uiState.update { it.copy(selectedInsight = insight) }
                }
            }
        }

        fun generateWeeklyReport() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                generateWeeklyReportUseCase()
                    .onSuccess { _uiState.update { state -> state.copy(isLoading = false, message = "Haftalik rapor hazir.") } }
                    .onFailure { _uiState.update { state -> state.copy(isLoading = false, message = it.message ?: "Rapor olusturulamadi.") } }
            }
        }

        fun detectPlateau() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                detectPlateauUseCase()
                    .onSuccess {
                        val message = if (it == null) "Plato sinyali bulunamadi." else "Plato analizi kaydedildi."
                        _uiState.update { state -> state.copy(isLoading = false, message = message) }
                    }.onFailure {
                        _uiState.update { state -> state.copy(isLoading = false, message = it.message ?: "Plato analizi basarisiz.") }
                    }
            }
        }

        fun markAsRead(insightId: String) {
            viewModelScope.launch {
                markAiInsightAsReadUseCase(insightId)
            }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }
    }
