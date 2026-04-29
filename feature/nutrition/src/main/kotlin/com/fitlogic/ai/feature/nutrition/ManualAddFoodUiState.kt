package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.MealType

data class ManualAddFoodUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val name: String = "",
    val kcalText: String = "",
    val proteinText: String = "",
    val carbText: String = "",
    val fatText: String = "",
    val gramsText: String = "100",
    val mealType: MealType = MealType.BREAKFAST,
    val saved: Boolean = false,
)
