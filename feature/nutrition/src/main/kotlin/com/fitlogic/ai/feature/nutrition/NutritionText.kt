package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.MealType

fun MealType.label(): String =
    when (this) {
        MealType.BREAKFAST -> "Kahvalti"
        MealType.LUNCH -> "Ogle"
        MealType.DINNER -> "Aksam"
        MealType.SNACK -> "Ara Ogun"
    }

fun mealTypeFromName(raw: String?): MealType {
    return runCatching { MealType.valueOf(raw ?: "") }.getOrDefault(MealType.BREAKFAST)
}
