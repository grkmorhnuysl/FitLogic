package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.usecase.nutrition.AddFoodEntryUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeNutritionRepository

    private val apple =
        FoodCatalogItem(
            id = "apple1",
            name = "Elma",
            kcalPer100g = 52f,
            proteinPer100g = 0.3f,
            carbPer100g = 14f,
            fatPer100g = 0.2f,
        )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository =
            FakeNutritionRepository().apply {
                foodById = apple
                addFoodEntryResult =
                    Result.success(
                        FoodEntry(
                            userId = "u1",
                            foodId = "apple1",
                            foodName = "Elma",
                            mealType = MealType.BREAKFAST,
                            grams = 150f,
                            kcal = 78f,
                            protein = 0.45f,
                            carb = 21f,
                            fat = 0.3f,
                        ),
                    )
            }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadFood_setsFoodInState() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()

            viewModel.loadFood("apple1", MealType.BREAKFAST)
            runCurrent()

            assertEquals(apple, viewModel.uiState.value.food)
        }

    @Test
    fun onGramsChange_recalculatesMacros() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            viewModel.loadFood("apple1", MealType.BREAKFAST)
            runCurrent()

            viewModel.onGramsChange("200")
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals("200", state.gramsText)
            assertEquals(104f, state.computedKcal, 0.1f)
            assertEquals(28f, state.computedCarb, 0.1f)
        }

    @Test
    fun saveEntry_setsSavedTrueOnSuccess() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            viewModel.loadFood("apple1", MealType.BREAKFAST)
            runCurrent()

            viewModel.saveEntry()
            runCurrent()

            assertTrue(viewModel.uiState.value.saved)
        }

    @Test
    fun saveEntry_invalidGrams_setsMessage() =
        runTest(dispatcher) {
            val viewModel = buildViewModel()
            viewModel.loadFood("apple1", MealType.BREAKFAST)
            runCurrent()

            viewModel.onGramsChange("abc")
            viewModel.saveEntry()
            runCurrent()

            assertNotNull(viewModel.uiState.value.message)
        }

    @Test
    fun loadFood_failure_setsMessage() =
        runTest(dispatcher) {
            repository.foodById = null
            val viewModel = buildViewModel()

            viewModel.loadFood("missing", MealType.BREAKFAST)
            runCurrent()

            assertNotNull(viewModel.uiState.value.message)
            assertNull(viewModel.uiState.value.food)
        }

    private fun buildViewModel(): FoodDetailViewModel =
        FoodDetailViewModel(
            addFoodEntryUseCase = AddFoodEntryUseCase(repository),
            nutritionRepository = repository,
        )
}
