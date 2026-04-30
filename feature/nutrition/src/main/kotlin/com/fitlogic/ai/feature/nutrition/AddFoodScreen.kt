@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlButton
import com.fitlogic.ai.core.designsystem.component.FlButtonVariant
import com.fitlogic.ai.core.designsystem.component.FlCard
import com.fitlogic.ai.core.designsystem.component.FlEmptyState
import com.fitlogic.ai.core.designsystem.component.FlLoadingIndicator
import com.fitlogic.ai.core.designsystem.component.FlTextField
import com.fitlogic.ai.core.domain.model.FoodCatalogItem
import com.fitlogic.ai.core.domain.model.MealType

private val addFoodTabs = listOf("Ara", "Barkod", "Favoriler", "Son")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    targetMealType: MealType,
    onBack: () -> Unit,
    onFoodClick: (foodId: String, mealType: MealType) -> Unit,
    onBarcodeClick: (MealType) -> Unit,
    onManualAddClick: (MealType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddFoodViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(targetMealType) {
        viewModel.setTargetMealType(targetMealType)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(state.navigateToFood) {
        val food = state.navigateToFood ?: return@LaunchedEffect
        viewModel.clearNavigateToFood()
        onFoodClick(food.id, targetMealType)
    }

    LaunchedEffect(state.navigateToManualAdd) {
        if (!state.navigateToManualAdd) return@LaunchedEffect
        viewModel.clearNavigateToManualAdd()
        onManualAddClick(targetMealType)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Yemek Ekle - ${targetMealType.label()}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            TabRow(selectedTabIndex = state.selectedTab) {
                addFoodTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.setTab(index) },
                        text = { Text(title) },
                    )
                }
            }

            when (state.selectedTab) {
                0 ->
                    SearchTab(
                        state = state,
                        viewModel = viewModel,
                        onFoodClick = { onFoodClick(it, targetMealType) },
                    )
                1 -> BarcodeTab(onBarcodeClick = { onBarcodeClick(targetMealType) })
                2 ->
                    FavoritesTab(
                        state = state,
                        viewModel = viewModel,
                        onFoodClick = { onFoodClick(it, targetMealType) },
                    )
                else -> RecentsTab(state = state, onFoodClick = { onFoodClick(it, targetMealType) })
            }
        }
    }
}

@Composable
private fun SearchTab(
    state: AddFoodUiState,
    viewModel: AddFoodViewModel,
    onFoodClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FlTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            label = "Yemek ara",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("nutrition_search_input"),
        )

        if (state.isLoading) {
            FlLoadingIndicator(modifier = Modifier.fillMaxSize())
        } else {
            FoodList(
                foods = state.searchResults,
                onFoodClick = onFoodClick,
                onToggleFavorite = { food ->
                    viewModel.toggleFavorite(food.id, !food.isFavorite)
                },
            )
        }
    }
}

@Composable
private fun BarcodeTab(onBarcodeClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Barkodu tarayarak hizli ekleme yapin.")
        FlButton(
            text = "Barkod Kamerasini Ac",
            onClick = onBarcodeClick,
            modifier = Modifier.fillMaxWidth().testTag("nutrition_open_barcode"),
        )
    }
}

@Composable
private fun FavoritesTab(
    state: AddFoodUiState,
    viewModel: AddFoodViewModel,
    onFoodClick: (String) -> Unit,
) {
    if (state.favorites.isEmpty()) {
        FlEmptyState(
            title = "Favori yok",
            description = "Detay ekraninda favoriye ekledikleriniz burada gorunur.",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.favorites, key = { it.id }) { food ->
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).clickable { onFoodClick(food.id) }) {
                        Text(food.name, style = MaterialTheme.typography.titleSmall)
                        Text("${food.kcalPer100g.toInt()} kcal/100g", style = MaterialTheme.typography.bodySmall)
                    }
                    FlButton(
                        text = "Hizli Ekle",
                        onClick = { viewModel.addFavoriteQuick(food.id) },
                        variant = FlButtonVariant.Secondary,
                        modifier = Modifier.testTag("nutrition_quick_add_favorite"),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentsTab(
    state: AddFoodUiState,
    onFoodClick: (String) -> Unit,
) {
    FoodList(
        foods = state.recents,
        onFoodClick = onFoodClick,
        onToggleFavorite = {},
    )
}

@Composable
private fun FoodList(
    foods: List<FoodCatalogItem>,
    onFoodClick: (String) -> Unit,
    onToggleFavorite: (FoodCatalogItem) -> Unit,
) {
    if (foods.isEmpty()) {
        FlEmptyState(
            title = "Yemek bulunamadi",
            description = "Arama ifadesini degistirip tekrar deneyin.",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(foods, key = { it.id }) { food ->
            FlCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("nutrition_search_item")
                        .clickable { onFoodClick(food.id) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(food.name, style = MaterialTheme.typography.titleSmall)
                        val macroSummary =
                            "${food.kcalPer100g.toInt()} kcal | " +
                                "P ${food.proteinPer100g.toInt()} " +
                                "C ${food.carbPer100g.toInt()} " +
                                "Y ${food.fatPer100g.toInt()}"
                        Text(
                            text = macroSummary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = { onToggleFavorite(food) }) {
                        Icon(
                            imageVector =
                                if (food.isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                            contentDescription = "Favori",
                        )
                    }
                }
            }
        }
    }
}
