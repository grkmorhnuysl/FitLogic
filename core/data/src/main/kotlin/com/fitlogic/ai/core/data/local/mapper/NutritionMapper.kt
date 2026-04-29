package com.fitlogic.ai.core.data.local.mapper

import com.fitlogic.ai.core.data.local.entity.FoodEntryEntity
import com.fitlogic.ai.core.data.local.entity.FoodsCatalogEntity
import com.fitlogic.ai.core.data.local.entity.WaterEntryEntity
import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.model.WaterEntry

fun FoodsCatalogEntity.toDomain(): FoodCatalogItem =
    FoodCatalogItem(
        id = id,
        name = name,
        brandName = brandName,
        barcode = barcode,
        kcalPer100g = kcalPer100g,
        proteinPer100g = proteinPer100g,
        carbPer100g = carbPer100g,
        fatPer100g = fatPer100g,
        source = source,
        isFavorite = isFavorite,
        lastUsedAt = lastUsedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun FoodEntryEntity.toDomain(): FoodEntry =
    FoodEntry(
        id = id,
        userId = userId,
        foodId = foodId,
        foodName = foodName,
        mealType = runCatching { enumValueOf<MealType>(mealType) }.getOrDefault(MealType.SNACK),
        grams = grams,
        kcal = kcal,
        protein = protein,
        carb = carb,
        fat = fat,
        eatenAt = eatenAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun WaterEntryEntity.toDomain(): WaterEntry =
    WaterEntry(
        id = id,
        userId = userId,
        amountMl = amountMl,
        consumedAt = consumedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
