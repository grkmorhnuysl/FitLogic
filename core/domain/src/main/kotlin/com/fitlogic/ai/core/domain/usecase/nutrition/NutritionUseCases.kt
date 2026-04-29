package com.fitlogic.ai.core.domain.usecase.nutrition

import com.fitlogic.ai.core.domain.model.DailyMacros
import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.model.WaterEntry
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddFoodEntryUseCase
    @Inject
    constructor(
        private val nutritionRepository: NutritionRepository,
    ) {
        suspend operator fun invoke(
            foodId: String,
            mealType: MealType,
            grams: Float,
            eatenAt: Long = System.currentTimeMillis(),
        ): Result<FoodEntry> = nutritionRepository.addFoodEntry(foodId, mealType, grams, eatenAt)
    }

class SearchFoodUseCase
    @Inject
    constructor(
        private val nutritionRepository: NutritionRepository,
    ) {
        suspend operator fun invoke(
            query: String,
            limit: Int = 30,
        ): Result<List<FoodCatalogItem>> = nutritionRepository.searchFoods(query = query, limit = limit)
    }

class ScanBarcodeUseCase
    @Inject
    constructor(
        private val nutritionRepository: NutritionRepository,
    ) {
        suspend operator fun invoke(barcode: String): Result<FoodCatalogItem?> = nutritionRepository.scanBarcode(barcode)
    }

class GetDailyMacrosUseCase
    @Inject
    constructor(
        private val nutritionRepository: NutritionRepository,
    ) {
        operator fun invoke(
            dayStartEpochMs: Long,
            dayEndEpochMs: Long,
        ): Flow<DailyMacros> = nutritionRepository.observeDailyMacros(dayStartEpochMs, dayEndEpochMs)
    }

class AddWaterUseCase
    @Inject
    constructor(
        private val nutritionRepository: NutritionRepository,
    ) {
        suspend operator fun invoke(
            amountMl: Int,
            consumedAt: Long = System.currentTimeMillis(),
        ): Result<WaterEntry> = nutritionRepository.addWater(amountMl, consumedAt)
    }
