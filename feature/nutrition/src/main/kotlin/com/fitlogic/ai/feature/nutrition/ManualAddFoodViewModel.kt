package com.fitlogic.ai.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.usecase.nutrition.AddFoodEntryUseCase
import com.fitlogic.ai.core.domain.usecase.nutrition.SearchFoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
class ManualAddFoodViewModel
    @Inject
    constructor(
        private val searchFoodUseCase: SearchFoodUseCase,
        private val addFoodEntryUseCase: AddFoodEntryUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ManualAddFoodUiState())
        val uiState: StateFlow<ManualAddFoodUiState> = _uiState.asStateFlow()

        fun setMealType(mealType: MealType) {
            _uiState.update { it.copy(mealType = mealType) }
        }

        fun onNameChange(value: String) {
            _uiState.update { it.copy(name = value, saved = false) }
        }

        fun onKcalChange(value: String) {
            _uiState.update { it.copy(kcalText = value) }
        }

        fun onProteinChange(value: String) {
            _uiState.update { it.copy(proteinText = value) }
        }

        fun onCarbChange(value: String) {
            _uiState.update { it.copy(carbText = value) }
        }

        fun onFatChange(value: String) {
            _uiState.update { it.copy(fatText = value) }
        }

        fun onGramsChange(value: String) {
            _uiState.update { it.copy(gramsText = value, saved = false) }
        }

        fun save() {
            val state = _uiState.value
            val name = state.name.trim()
            val grams = state.gramsText.replace(',', '.').toFloatOrNull()
            if (name.isBlank() || grams == null || grams <= 0f) {
                _uiState.update { it.copy(message = "Yemek adi ve gecerli gram bilgisi gerekli.") }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val searchResult = searchFoodUseCase(name, limit = 1)
                val food = searchResult.getOrNull()?.firstOrNull()
                if (food == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = "Manuel kayit icin eslesen yemek bulunamadi. Aramayi degistirip tekrar deneyin.",
                        )
                    }
                    return@launch
                }

                addFoodEntryUseCase(food.id, state.mealType, grams).fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                saved = true,
                                message = "Yemek eklendi. Besin degerleri katalog kaydindan alindi.",
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, message = error.message ?: "Yemek eklenemedi.") }
                    },
                )
            }
        }

        fun clearSaved() {
            _uiState.update { it.copy(saved = false) }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }
    }
