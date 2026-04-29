package com.fitlogic.ai.feature.nutrition

import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType
import com.fitlogic.ai.core.domain.usecase.nutrition.AddFoodEntryUseCase
import com.fitlogic.ai.core.domain.usecase.nutrition.ScanBarcodeUseCase
import com.fitlogic.ai.core.domain.usecase.nutrition.SearchFoodUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddFoodViewModelTest {
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
    fun search_updatesResults() =
        runTest(dispatcher) {
            val apple =
                FoodCatalogItem(
                    name = "Elma",
                    kcalPer100g = 52f,
                    proteinPer100g = 0.3f,
                    carbPer100g = 14f,
                    fatPer100g = 0.2f,
                )
            repository.searchResult = listOf(apple)
            val viewModel = buildViewModel()

            viewModel.onSearchQueryChange("el")
            runCurrent()

            assertEquals(1, viewModel.uiState.value.searchResults.size)
        }

    @Test
    fun barcode_found_setsNavigateToFood() =
        runTest(dispatcher) {
            val apple =
                FoodCatalogItem(
                    id = "f1",
                    name = "Elma",
                    kcalPer100g = 52f,
                    proteinPer100g = 0.3f,
                    carbPer100g = 14f,
                    fatPer100g = 0.2f,
                )
            repository.barcodeResult = apple
            val viewModel = buildViewModel()

            viewModel.onBarcodeDetected("869")
            runCurrent()

            assertEquals("f1", viewModel.uiState.value.navigateToFood?.id)
        }

    @Test
    fun barcode_notFound_setsManualNavigation() =
        runTest(dispatcher) {
            repository.barcodeResult = null
            val viewModel = buildViewModel()

            viewModel.onBarcodeDetected("000")
            runCurrent()

            assertTrue(viewModel.uiState.value.navigateToManualAdd)
        }

    @Test
    fun addFavoriteQuick_failure_setsMessage() =
        runTest(dispatcher) {
            repository.addFoodEntryResult = Result.failure(Exception("cannot add"))
            val viewModel = buildViewModel()

            viewModel.addFavoriteQuick("f1")
            runCurrent()

            assertNotNull(viewModel.uiState.value.message)
        }

    private fun buildViewModel(): AddFoodViewModel {
        repository.addFoodEntryResult =
            Result.success(
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
                ),
            )
        return AddFoodViewModel(
            searchFoodUseCase = SearchFoodUseCase(repository),
            scanBarcodeUseCase = ScanBarcodeUseCase(repository),
            addFoodEntryUseCase = AddFoodEntryUseCase(repository),
            nutritionRepository = repository,
        )
    }
}
