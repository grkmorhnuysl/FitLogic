package com.fitlogic.ai.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import com.fitlogic.ai.core.domain.usecase.nutrition.AddWaterUseCase
import com.fitlogic.ai.core.domain.usecase.nutrition.GetDailyMacrosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel
    @Inject
    constructor(
        private val getDailyMacrosUseCase: GetDailyMacrosUseCase,
        private val addWaterUseCase: AddWaterUseCase,
        private val nutritionRepository: NutritionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NutritionUiState())
        val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

        init {
            val (start, end) = todayEpochRange()
            observeDailyMacros(start, end)
            observeFoodEntries(start, end)
            seedCatalog()
        }

        fun addWater(amountMl: Int) {
            launchWithLoading {
                addWaterUseCase(amountMl).fold(
                    onSuccess = { _uiState.update { it.copy(message = null) } },
                    onFailure = { error ->
                        _uiState.update { it.copy(message = error.message ?: "Su eklenemedi.") }
                    },
                )
            }
        }

        fun setQuickWaterAmount(amountMl: Int) {
            _uiState.update { it.copy(quickWaterAmountMl = amountMl) }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }

        private fun observeDailyMacros(
            start: Long,
            end: Long,
        ) {
            viewModelScope.launch {
                getDailyMacrosUseCase(start, end)
                    .catch { error ->
                        _uiState.update { it.copy(message = error.message ?: "Makrolar yuklenemedi.") }
                    }
                    .collect { macros ->
                        _uiState.update { it.copy(dailyMacros = macros) }
                    }
            }
        }

        private fun observeFoodEntries(
            start: Long,
            end: Long,
        ) {
            viewModelScope.launch {
                nutritionRepository.observeFoodEntries(start, end)
                    .catch { error ->
                        _uiState.update { it.copy(message = error.message ?: "Kayitlar yuklenemedi.") }
                    }
                    .collect { entries ->
                        _uiState.update { it.copy(foodEntriesByMeal = entries.groupBy { entry -> entry.mealType }) }
                    }
            }
        }

        private fun seedCatalog() {
            viewModelScope.launch {
                nutritionRepository.ensureFoodCatalogSeeded().onFailure { error ->
                    _uiState.update { it.copy(message = error.message ?: "Besin katalogu hazirlanamadi.") }
                }
            }
        }

        private fun launchWithLoading(block: suspend () -> Unit) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                runCatching { block() }
                    .onFailure { error ->
                        _uiState.update { it.copy(message = error.message ?: "Beklenmeyen hata") }
                    }
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        private fun todayEpochRange(): Pair<Long, Long> {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val end = calendar.timeInMillis - 1
            return start to end
        }
    }
