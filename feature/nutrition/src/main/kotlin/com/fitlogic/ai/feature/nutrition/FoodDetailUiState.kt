package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.MealType

data class FoodDetailUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val food: FoodCatalogItem? = null,
    val gramsText: String = "100",
    val mealType: MealType = MealType.BREAKFAST,
    val computedKcal: Float = 0f,
    val computedProtein: Float = 0f,
    val computedCarb: Float = 0f,
    val computedFat: Float = 0f,
    val saved: Boolean = false,
)
