@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlButton
import com.fitlogic.ai.core.designsystem.component.FlCard
import com.fitlogic.ai.core.designsystem.component.FlEmptyState
import com.fitlogic.ai.core.designsystem.component.FlLoadingIndicator
import com.fitlogic.ai.core.domain.model.DailyMacros
import com.fitlogic.ai.core.domain.model.FoodEntry
import com.fitlogic.ai.core.domain.model.MealType

@Composable
fun NutritionScreen(
    onAddFoodClick: (MealType) -> Unit = {},
    onOpenBarcode: (MealType) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading && state.dailyMacros == null -> {
                FlLoadingIndicator(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                )
            }

            else -> {
                NutritionContent(
                    state = state,
                    onAddFoodClick = onAddFoodClick,
                    onOpenBarcode = onOpenBarcode,
                    onAddWater = viewModel::addWater,
                    onSetQuickWater = viewModel::setQuickWaterAmount,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun NutritionContent(
    state: NutritionUiState,
    onAddFoodClick: (MealType) -> Unit,
    onOpenBarcode: (MealType) -> Unit,
    onAddWater: (Int) -> Unit,
    onSetQuickWater: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val macros = state.dailyMacros
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (macros != null) {
                MacroCards(macros)
            } else {
                FlEmptyState(
                    title = "Makro ozeti hazir degil",
                    description = "Bugun icin kayitlar geldikce ozet guncellenecek.",
                )
            }
        }

        item {
            WaterCard(
                totalMl = macros?.totalWaterMl ?: 0,
                quickAmount = state.quickWaterAmountMl,
                onSetQuickWater = onSetQuickWater,
                onAddWater = onAddWater,
            )
        }

        MealType.entries.forEach { mealType ->
            item {
                MealSection(
                    mealType = mealType,
                    entries = state.foodEntriesByMeal[mealType].orEmpty(),
                    onAddFoodClick = { onAddFoodClick(mealType) },
                    onOpenBarcode = { onOpenBarcode(mealType) },
                )
            }
        }
    }
}

@Composable
private fun MacroCards(macros: DailyMacros) {
    FlCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Gunluk Makrolar", style = MaterialTheme.typography.titleMedium)
            MacroRow(
                name = "Kalori",
                value = macros.consumedCalories,
                target = macros.targetCalories,
            )
            MacroRow(
                name = "Protein",
                value = macros.consumedProtein,
                target = macros.targetProtein,
            )
            MacroRow(
                name = "Karbonhidrat",
                value = macros.consumedCarb,
                target = macros.targetCarb,
            )
            MacroRow(
                name = "Yag",
                value = macros.consumedFat,
                target = macros.targetFat,
            )
        }
    }
}

@Composable
private fun MacroRow(
    name: String,
    value: Float,
    target: Float?,
) {
    val progress = if (target != null && target > 0f) (value / target).coerceIn(0f, 1f) else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            val text =
                if (target != null) {
                    "${value.toInt()} / ${target.toInt()}"
                } else {
                    value.toInt().toString()
                }
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
        CircularProgressIndicator(progress = { progress })
    }
}

@Composable
private fun WaterCard(
    totalMl: Int,
    quickAmount: Int,
    onSetQuickWater: (Int) -> Unit,
    onAddWater: (Int) -> Unit,
) {
    FlCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Su Takibi", style = MaterialTheme.typography.titleMedium)
            Text(text = "Toplam: $totalMl ml", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlButton(
                    text = "200 ml",
                    onClick = { onSetQuickWater(200) },
                    variant = com.fitlogic.ai.core.designsystem.component.FlButtonVariant.Secondary,
                )
                FlButton(
                    text = "250 ml",
                    onClick = { onSetQuickWater(250) },
                    variant = com.fitlogic.ai.core.designsystem.component.FlButtonVariant.Secondary,
                )
                FlButton(
                    text = "500 ml",
                    onClick = { onSetQuickWater(500) },
                    variant = com.fitlogic.ai.core.designsystem.component.FlButtonVariant.Secondary,
                )
            }
            FlButton(
                text = "+$quickAmount ml su ekle",
                onClick = { onAddWater(quickAmount) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MealSection(
    mealType: MealType,
    entries: List<FoodEntry>,
    onAddFoodClick: () -> Unit,
    onOpenBarcode: () -> Unit,
) {
    FlCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = mealType.label(), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlButton(text = "Yemek Ekle", onClick = onAddFoodClick)
                FlButton(
                    text = "Barkod Tara",
                    onClick = onOpenBarcode,
                    variant = com.fitlogic.ai.core.designsystem.component.FlButtonVariant.Secondary,
                )
            }
            if (entries.isEmpty()) {
                Text(
                    text = "Henuz kayit yok.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                entries.forEach { entry ->
                    Text(
                        text = "${entry.foodName} - ${entry.grams.toInt()}g (${entry.kcal.toInt()} kcal)",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
