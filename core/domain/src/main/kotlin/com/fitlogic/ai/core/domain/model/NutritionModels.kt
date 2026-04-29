package com.fitlogic.ai.core.domain.model

import java.util.UUID

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
}

data class FoodCatalogItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val brandName: String? = null,
    val barcode: String? = null,
    val kcalPer100g: Float,
    val proteinPer100g: Float,
    val carbPer100g: Float,
    val fatPer100g: Float,
    val source: String = "LOCAL",
    val isFavorite: Boolean = false,
    val lastUsedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class FoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val foodId: String,
    val foodName: String,
    val mealType: MealType,
    val grams: Float,
    val kcal: Float,
    val protein: Float,
    val carb: Float,
    val fat: Float,
    val eatenAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class WaterEntry(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val amountMl: Int,
    val consumedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class DailyMacros(
    val consumedCalories: Float,
    val consumedProtein: Float,
    val consumedCarb: Float,
    val consumedFat: Float,
    val targetCalories: Float? = null,
    val targetProtein: Float? = null,
    val targetCarb: Float? = null,
    val targetFat: Float? = null,
    val totalWaterMl: Int = 0,
) {
    val remainingCalories: Float? = targetCalories?.minus(consumedCalories)
    val remainingProtein: Float? = targetProtein?.minus(consumedProtein)
    val remainingCarb: Float? = targetCarb?.minus(consumedCarb)
    val remainingFat: Float? = targetFat?.minus(consumedFat)
}
