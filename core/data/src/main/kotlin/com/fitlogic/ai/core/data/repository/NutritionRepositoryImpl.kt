package com.fitlogic.ai.core.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.fitlogic.ai.core.data.local.FitLogicDatabase
import com.fitlogic.ai.core.data.local.dao.FoodEntryDao
import com.fitlogic.ai.core.data.local.dao.FoodsCatalogDao
import com.fitlogic.ai.core.data.local.dao.UserDao
import com.fitlogic.ai.core.data.local.dao.WaterEntryDao
import com.fitlogic.ai.core.data.local.entity.FoodEntryEntity
import com.fitlogic.ai.core.data.local.entity.FoodsCatalogEntity
import com.fitlogic.ai.core.data.local.entity.WaterEntryEntity
import com.fitlogic.ai.core.data.local.mapper.toDomain
import com.fitlogic.ai.core.data.local.session.SessionPreferences
import com.fitlogic.ai.core.data.remote.OpenFoodFactsClient
import com.fitlogic.ai.core.domain.model.DailyMacros
import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.model.WaterEntry
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("LongParameterList", "TooManyFunctions")
class NutritionRepositoryImpl
    @Inject
    constructor(
        private val database: FitLogicDatabase,
        private val foodsCatalogDao: FoodsCatalogDao,
        private val foodEntryDao: FoodEntryDao,
        private val waterEntryDao: WaterEntryDao,
        private val userDao: UserDao,
        private val sessionPreferences: SessionPreferences,
        private val openFoodFactsClient: OpenFoodFactsClient,
        @ApplicationContext private val context: Context,
    ) : NutritionRepository {
        override fun observeFoodEntries(
            dayStartEpochMs: Long,
            dayEndEpochMs: Long,
        ): Flow<List<FoodEntry>> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf(emptyList())
                foodEntryDao.observeByDay(
                    userId = userId,
                    dayStartEpochMs = dayStartEpochMs,
                    dayEndEpochMs = dayEndEpochMs,
                ).map { entries -> entries.map(FoodEntryEntity::toDomain) }
            }

        override fun observeDailyMacros(
            dayStartEpochMs: Long,
            dayEndEpochMs: Long,
        ): Flow<DailyMacros> =
            sessionPreferences.sessionSnapshot.flatMapLatest { snapshot ->
                val userId = snapshot.currentUserId ?: return@flatMapLatest flowOf(DailyMacros(0f, 0f, 0f, 0f))
                combine(
                    foodEntryDao.observeDailyTotals(
                        userId = userId,
                        dayStartEpochMs = dayStartEpochMs,
                        dayEndEpochMs = dayEndEpochMs,
                    ),
                    waterEntryDao.observeDailyTotalMl(
                        userId = userId,
                        dayStartEpochMs = dayStartEpochMs,
                        dayEndEpochMs = dayEndEpochMs,
                    ),
                    userDao.observeById(userId),
                ) { foodTotals, waterTotalMl, userEntity ->
                    DailyMacros(
                        consumedCalories = foodTotals.calories,
                        consumedProtein = foodTotals.protein,
                        consumedCarb = foodTotals.carb,
                        consumedFat = foodTotals.fat,
                        targetCalories = userEntity?.targetCalories?.toFloat(),
                        targetProtein = userEntity?.proteinGrams?.toFloat(),
                        targetCarb = userEntity?.carbGrams?.toFloat(),
                        targetFat = userEntity?.fatGrams?.toFloat(),
                        totalWaterMl = waterTotalMl,
                    )
                }
            }

        override fun observeFavoriteFoods(): Flow<List<FoodCatalogItem>> =
            foodsCatalogDao.observeFavorites().map { items -> items.map(FoodsCatalogEntity::toDomain) }

        override suspend fun ensureFoodCatalogSeeded(): Result<Unit> =
            runCatching {
                if (foodsCatalogDao.count() > 0) return@runCatching
                val json = context.assets.open(SEED_FILE).bufferedReader().use { it.readText() }
                val records = jsonParser.parseToJsonElement(json).jsonArray
                val now = System.currentTimeMillis()
                val entities =
                    records.mapNotNull { element ->
                        val item = element.jsonObject
                        val name = item["name"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                        if (name.isBlank()) return@mapNotNull null

                        FoodsCatalogEntity(
                            id = item["id"]?.jsonPrimitive?.contentOrNull ?: UUID.randomUUID().toString(),
                            name = name,
                            brandName = item["brandName"]?.jsonPrimitive?.contentOrNull,
                            barcode = item["barcode"]?.jsonPrimitive?.contentOrNull,
                            kcalPer100g = item.float("kcalPer100g") ?: 0f,
                            proteinPer100g = item.float("proteinPer100g") ?: 0f,
                            carbPer100g = item.float("carbPer100g") ?: 0f,
                            fatPer100g = item.float("fatPer100g") ?: 0f,
                            source = "LOCAL",
                            createdAt = now,
                            updatedAt = now,
                            syncStatus = "SYNCED",
                        )
                    }
                foodsCatalogDao.insertAll(entities)
            }

        override suspend fun searchFoods(
            query: String,
            limit: Int,
        ): Result<List<FoodCatalogItem>> =
            runCatching {
                ensureFoodCatalogSeeded().getOrThrow()
                val normalizedQuery = query.trim()
                if (normalizedQuery.isBlank()) return@runCatching emptyList()

                val localResults = foodsCatalogDao.search(query = normalizedQuery, limit = limit).toMutableList()
                val missingCount = (limit - localResults.size).coerceAtLeast(0)
                if (missingCount > 0) {
                    val remoteItems =
                        openFoodFactsClient.searchFoods(
                            query = normalizedQuery,
                            limit = missingCount,
                        ).getOrDefault(emptyList())
                    remoteItems.forEach { remote ->
                        val now = System.currentTimeMillis()
                        val entity = remote.toEntity(now)
                        foodsCatalogDao.upsert(entity)
                        if (localResults.none { it.id == entity.id }) {
                            localResults += entity
                        }
                    }
                }
                localResults.take(limit).map(FoodsCatalogEntity::toDomain)
            }

        override suspend fun scanBarcode(barcode: String): Result<FoodCatalogItem?> =
            runCatching {
                ensureFoodCatalogSeeded().getOrThrow()
                val normalizedBarcode = barcode.trim()
                check(normalizedBarcode.isNotEmpty()) { "Barkod bos olamaz." }

                foodsCatalogDao.getByBarcode(normalizedBarcode)?.toDomain()?.let { return@runCatching it }

                val remoteFood =
                    openFoodFactsClient.lookupByBarcode(normalizedBarcode).getOrThrow()
                        ?: return@runCatching null
                val now = System.currentTimeMillis()
                val entity = remoteFood.toEntity(now)
                foodsCatalogDao.upsert(entity)
                entity.toDomain()
            }

        override suspend fun getFoodById(foodId: String): Result<FoodCatalogItem> =
            runCatching {
                ensureFoodCatalogSeeded().getOrThrow()
                checkNotNull(foodsCatalogDao.getById(foodId)) { "Yemek bulunamadi." }.toDomain()
            }

        override suspend fun addFoodEntry(
            foodId: String,
            mealType: MealType,
            grams: Float,
            eatenAt: Long,
        ): Result<FoodEntry> =
            runCatching {
                check(grams > 0f) { "Porsiyon gram sifirdan buyuk olmali." }
                val userId = requireCurrentUserId()
                val food = checkNotNull(foodsCatalogDao.getById(foodId)) { "Yemek bulunamadi." }
                val multiplier = grams / 100f
                val now = System.currentTimeMillis()
                val entry =
                    FoodEntryEntity(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        foodId = food.id,
                        foodName = food.name,
                        mealType = mealType.name,
                        grams = grams,
                        kcal = (food.kcalPer100g * multiplier).coerceAtLeast(0f),
                        protein = (food.proteinPer100g * multiplier).coerceAtLeast(0f),
                        carb = (food.carbPer100g * multiplier).coerceAtLeast(0f),
                        fat = (food.fatPer100g * multiplier).coerceAtLeast(0f),
                        eatenAt = eatenAt,
                        createdAt = now,
                        updatedAt = now,
                    )
                database.withTransaction {
                    foodEntryDao.insert(entry)
                    foodsCatalogDao.touchLastUsed(
                        foodId = food.id,
                        lastUsedAt = now,
                        updatedAt = now,
                    )
                }
                entry.toDomain()
            }

        override suspend fun addWater(
            amountMl: Int,
            consumedAt: Long,
        ): Result<WaterEntry> =
            runCatching {
                check(amountMl > 0) { "Su miktari sifirdan buyuk olmali." }
                val userId = requireCurrentUserId()
                val now = System.currentTimeMillis()
                val entry =
                    WaterEntryEntity(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        amountMl = amountMl,
                        consumedAt = consumedAt,
                        createdAt = now,
                        updatedAt = now,
                    )
                waterEntryDao.insert(entry)
                entry.toDomain()
            }

        override suspend fun setFoodFavorite(
            foodId: String,
            favorite: Boolean,
        ): Result<Unit> =
            runCatching {
                checkNotNull(foodsCatalogDao.getById(foodId)) { "Favori islemi icin yemek bulunamadi." }
                foodsCatalogDao.setFavorite(
                    foodId = foodId,
                    favorite = favorite,
                    updatedAt = System.currentTimeMillis(),
                )
            }

        private suspend fun requireCurrentUserId(): String {
            val snapshot = sessionPreferences.sessionSnapshot.first()
            return checkNotNull(snapshot.currentUserId) { "Aktif kullanici bulunamadi." }
        }

        private fun com.fitlogic.ai.core.data.remote.OpenFoodFactsFood.toEntity(now: Long): FoodsCatalogEntity =
            FoodsCatalogEntity(
                id = buildStableId(),
                name = name,
                brandName = brandName,
                barcode = barcode,
                kcalPer100g = kcalPer100g,
                proteinPer100g = proteinPer100g,
                carbPer100g = carbPer100g,
                fatPer100g = fatPer100g,
                source = "OPEN_FOOD_FACTS",
                createdAt = now,
                updatedAt = now,
                syncStatus = "SYNCED",
            )

        private fun com.fitlogic.ai.core.data.remote.OpenFoodFactsFood.buildStableId(): String =
            barcode?.takeIf { it.isNotBlank() } ?: "off-${id.ifBlank { UUID.randomUUID().toString() }}"

        private fun kotlinx.serialization.json.JsonObject.float(key: String): Float? {
            val raw = this[key]?.jsonPrimitive?.contentOrNull ?: return null
            return raw.toFloatOrNull()
        }

        companion object {
            private const val SEED_FILE = "nutrition_foods_seed.json"
            private val jsonParser = Json { ignoreUnknownKeys = true }
        }
    }
