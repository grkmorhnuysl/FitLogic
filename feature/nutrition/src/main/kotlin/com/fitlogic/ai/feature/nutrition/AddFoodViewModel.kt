package com.fitlogic.ai.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import com.fitlogic.ai.core.domain.usecase.nutrition.AddFoodEntryUseCase
import com.fitlogic.ai.core.domain.usecase.nutrition.ScanBarcodeUseCase
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
class AddFoodViewModel
    @Inject
    constructor(
        private val searchFoodUseCase: SearchFoodUseCase,
        private val scanBarcodeUseCase: ScanBarcodeUseCase,
        private val addFoodEntryUseCase: AddFoodEntryUseCase,
        private val nutritionRepository: NutritionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AddFoodUiState())
        val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

        init {
            observeFavorites()
            searchFoods()
        }

        fun setTab(index: Int) {
            _uiState.update { it.copy(selectedTab = index) }
        }

        fun setTargetMealType(mealType: MealType) {
            _uiState.update { it.copy(targetMealType = mealType) }
        }

        fun onSearchQueryChange(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
            searchFoods()
        }

        fun onBarcodeDetected(barcode: String) {
            launchWithLoading {
                scanBarcodeUseCase(barcode).fold(
                    onSuccess = { food ->
                        if (food == null) {
                            _uiState.update { it.copy(navigateToManualAdd = true) }
                        } else {
                            _uiState.update { it.copy(navigateToFood = food) }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(message = error.message ?: "Barkod okunamadi.") }
                    },
                )
            }
        }

        fun toggleFavorite(
            foodId: String,
            favorite: Boolean,
        ) {
            viewModelScope.launch {
                nutritionRepository.setFoodFavorite(foodId, favorite).onFailure { error ->
                    _uiState.update { it.copy(message = error.message ?: "Favori guncellenemedi.") }
                }
            }
        }

        fun addFavoriteQuick(foodId: String) {
            val mealType = _uiState.value.targetMealType
            launchWithLoading {
                addFoodEntryUseCase(foodId = foodId, mealType = mealType, grams = 100f).fold(
                    onSuccess = { _uiState.update { it.copy(message = "Yemek eklendi.") } },
                    onFailure = { error ->
                        _uiState.update { it.copy(message = error.message ?: "Yemek eklenemedi.") }
                    },
                )
            }
        }

        fun clearNavigateToFood() {
            _uiState.update { it.copy(navigateToFood = null) }
        }

        fun clearNavigateToManualAdd() {
            _uiState.update { it.copy(navigateToManualAdd = false) }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }

        private fun observeFavorites() {
            viewModelScope.launch {
                nutritionRepository.observeFavoriteFoods().collect { favorites ->
                    _uiState.update { it.copy(favorites = favorites) }
                }
            }
        }

        private fun searchFoods() {
            viewModelScope.launch {
                val query = _uiState.value.searchQuery.trim()
                val result = searchFoodUseCase(query)
                _uiState.update {
                    it.copy(
                        searchResults = result.getOrDefault(emptyList()),
                        message = result.exceptionOrNull()?.message ?: it.message,
                    )
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
    }
