package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.DailyMacros
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.model.WaterEntry
import com.fitlogic.ai.core.domain.usecase.nutrition.AddWaterUseCase
import com.fitlogic.ai.core.domain.usecase.nutrition.GetDailyMacrosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeNutritionRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeNutritionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dailyMacros_updatesWhenRepositoryEmits() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            val macros = DailyMacros(500f, 30f, 60f, 15f)

            repository.dailyMacrosFlow.emit(macros)
            runCurrent()

            assertEquals(macros, viewModel.uiState.value.dailyMacros)
        }

    @Test
    fun foodEntries_groupedByMealType() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            val entry =
                FoodEntry(
                    userId = "u1",
                    foodId = "f1",
                    foodName = "Elma",
                    mealType = MealType.BREAKFAST,
                    grams = 100f,
                    kcal = 52f,
                    protein = 0.3f,
                    carb = 14f,
                    fat = 0.2f,
                )

            repository.foodEntriesFlow.emit(listOf(entry))
            runCurrent()

            assertEquals(1, viewModel.uiState.value.foodEntriesByMeal[MealType.BREAKFAST]?.size)
        }

    @Test
    fun addWater_failureSetsMessage() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            repository.addWaterResult = Result.failure(Exception("DB error"))

            viewModel.addWater(250)
            runCurrent()

            assertNotNull(viewModel.uiState.value.message)
        }

    @Test
    fun clearMessage_setsNull() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            repository.addWaterResult = Result.success(WaterEntry(userId = "u1", amountMl = 250))

            viewModel.addWater(250)
            runCurrent()
            viewModel.clearMessage()

            assertNull(viewModel.uiState.value.message)
        }

    private fun buildViewModel(): NutritionViewModel =
        NutritionViewModel(
            getDailyMacrosUseCase = GetDailyMacrosUseCase(repository),
            addWaterUseCase = AddWaterUseCase(repository),
            nutritionRepository = repository,
        )
}
