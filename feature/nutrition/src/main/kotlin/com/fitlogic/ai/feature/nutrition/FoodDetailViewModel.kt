package com.fitlogic.ai.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import com.fitlogic.ai.core.domain.usecase.nutrition.AddFoodEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodDetailViewModel
    @Inject
    constructor(
        private val addFoodEntryUseCase: AddFoodEntryUseCase,
        private val nutritionRepository: NutritionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(FoodDetailUiState())
        val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

        fun loadFood(
            foodId: String,
            mealType: MealType,
        ) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, mealType = mealType, saved = false) }
                nutritionRepository.getFoodById(foodId).fold(
                    onSuccess = { food ->
                        _uiState.update { it.copy(isLoading = false, food = food) }
                        recalculateMacros(_uiState.value)
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false, message = error.message ?: "Yemek yuklenemedi.") }
                    },
                )
            }
        }

        fun onGramsChange(gramsText: String) {
            _uiState.update { it.copy(gramsText = gramsText, saved = false) }
            recalculateMacros(_uiState.value)
        }

        fun onMealTypeChange(mealType: MealType) {
            _uiState.update { it.copy(mealType = mealType) }
        }

        fun saveEntry() {
            val state = _uiState.value
            val food = state.food ?: return
            val grams = state.gramsText.replace(',', '.').toFloatOrNull()
            if (grams == null || grams <= 0f) {
                _uiState.update { it.copy(message = "Gecerli bir miktar girin.") }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                addFoodEntryUseCase(food.id, state.mealType, grams).fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, saved = true, message = "Yemek eklendi.") }
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

        private fun recalculateMacros(state: FoodDetailUiState) {
            val food = state.food ?: return
            val grams = state.gramsText.replace(',', '.').toFloatOrNull() ?: return
            val factor = grams / 100f
            _uiState.update {
                it.copy(
                    computedKcal = food.kcalPer100g * factor,
                    computedProtein = food.proteinPer100g * factor,
                    computedCarb = food.carbPer100g * factor,
                    computedFat = food.fatPer100g * factor,
                )
            }
        }
    }
