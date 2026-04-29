package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.MealType

data class AddFoodUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val searchResults: List<FoodCatalogItem> = emptyList(),
    val favorites: List<FoodCatalogItem> = emptyList(),
    val recents: List<FoodCatalogItem> = emptyList(),
    val targetMealType: MealType = MealType.BREAKFAST,
    val navigateToFood: FoodCatalogItem? = null,
    val navigateToManualAdd: Boolean = false,
)
