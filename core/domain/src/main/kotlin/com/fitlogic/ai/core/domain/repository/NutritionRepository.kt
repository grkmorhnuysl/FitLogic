package com.fitlogic.ai.core.domain.repository

import com.fitlogic.ai.core.domain.model.DailyMacros
import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.model.WaterEntry
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun observeFoodEntries(
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<List<FoodEntry>>

    fun observeDailyMacros(
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<DailyMacros>

    fun observeFavoriteFoods(): Flow<List<FoodCatalogItem>>

    suspend fun ensureFoodCatalogSeeded(): Result<Unit>

    suspend fun searchFoods(
        query: String,
        limit: Int = 30,
    ): Result<List<FoodCatalogItem>>

    suspend fun scanBarcode(barcode: String): Result<FoodCatalogItem?>

    suspend fun getFoodById(foodId: String): Result<FoodCatalogItem>

    suspend fun addFoodEntry(
        foodId: String,
        mealType: MealType,
        grams: Float,
        eatenAt: Long = System.currentTimeMillis(),
    ): Result<FoodEntry>

    suspend fun addWater(
        amountMl: Int,
        consumedAt: Long = System.currentTimeMillis(),
    ): Result<WaterEntry>

    suspend fun setFoodFavorite(
        foodId: String,
        favorite: Boolean,
    ): Result<Unit>
}
