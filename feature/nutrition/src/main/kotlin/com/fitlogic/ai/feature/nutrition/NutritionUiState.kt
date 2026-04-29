package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.DailyMacros
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType

data class NutritionUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val dailyMacros: DailyMacros? = null,
    val foodEntriesByMeal: Map<MealType, List<FoodEntry>> = emptyMap(),
    val quickWaterAmountMl: Int = 250,
)
