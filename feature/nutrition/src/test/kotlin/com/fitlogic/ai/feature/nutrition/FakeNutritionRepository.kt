package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.DailyMacros
import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.model.WaterEntry
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNutritionRepository : NutritionRepository {
    val dailyMacrosFlow = MutableStateFlow(DailyMacros(0f, 0f, 0f, 0f))
    val foodEntriesFlow = MutableStateFlow<List<FoodEntry>>(emptyList())
    val favoritesFlow = MutableStateFlow<List<FoodCatalogItem>>(emptyList())
    var searchResult: List<FoodCatalogItem> = emptyList()
    var barcodeResult: FoodCatalogItem? = null
    var foodById: FoodCatalogItem? = null
    var addFoodEntryResult: Result<FoodEntry> = Result.failure(IllegalStateException("not configured"))
    var addWaterResult: Result<WaterEntry> = Result.failure(IllegalStateException("not configured"))

    override fun observeFoodEntries(
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<List<FoodEntry>> = foodEntriesFlow

    override fun observeDailyMacros(
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<DailyMacros> = dailyMacrosFlow

    override fun observeFavoriteFoods(): Flow<List<FoodCatalogItem>> = favoritesFlow

    override suspend fun ensureFoodCatalogSeeded(): Result<Unit> = Result.success(Unit)

    override suspend fun searchFoods(
        query: String,
        limit: Int,
    ): Result<List<FoodCatalogItem>> = Result.success(searchResult)

    override suspend fun scanBarcode(barcode: String): Result<FoodCatalogItem?> = Result.success(barcodeResult)

    override suspend fun getFoodById(foodId: String): Result<FoodCatalogItem> {
        val food = foodById ?: return Result.failure(NoSuchElementException(foodId))
        return Result.success(food)
    }

    override suspend fun addFoodEntry(
        foodId: String,
        mealType: MealType,
        grams: Float,
        eatenAt: Long,
    ): Result<FoodEntry> = addFoodEntryResult

    override suspend fun addWater(
        amountMl: Int,
        consumedAt: Long,
    ): Result<WaterEntry> = addWaterResult

    override suspend fun setFoodFavorite(
        foodId: String,
        favorite: Boolean,
    ): Result<Unit> = Result.success(Unit)
}
