@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlButton
import com.fitlogic.ai.core.designsystem.component.FlCard
import com.fitlogic.ai.core.designsystem.component.FlLoadingIndicator
import com.fitlogic.ai.core.designsystem.component.FlTextField
import com.fitlogic.ai.core.domain.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun FoodDetailScreen(
    foodId: String,
    mealType: MealType,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(foodId, mealType) {
        viewModel.loadFood(foodId, mealType)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(state.saved) {
        if (!state.saved) return@LaunchedEffect
        viewModel.clearSaved()
        onSaved()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.food?.name ?: "Yemek Detay") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading && state.food == null -> FlLoadingIndicator(modifier = Modifier.fillMaxSize())
            state.food == null ->
                Text(
                    text = "Yemek yuklenemedi.",
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                )
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FlCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Porsiyon", style = MaterialTheme.typography.titleSmall)
                            FlTextField(
                                value = state.gramsText,
                                onValueChange = viewModel::onGramsChange,
                                label = "Gram",
                                modifier = Modifier.fillMaxWidth(),
                            )
                            MealTypeSelector(
                                selected = state.mealType,
                                onSelect = viewModel::onMealTypeChange,
                            )
                        }
                    }

                    FlCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("Makro Ozeti", style = MaterialTheme.typography.titleSmall)
                            Text("Kalori: ${state.computedKcal.toInt()} kcal")
                            Text("Protein: ${"%.1f".format(state.computedProtein)} g")
                            Text("Karbonhidrat: ${"%.1f".format(state.computedCarb)} g")
                            Text("Yag: ${"%.1f".format(state.computedFat)} g")
                        }
                    }

                    FlButton(
                        text = if (state.isLoading) "Kaydediliyor..." else "Kaydet",
                        onClick = viewModel::saveEntry,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MealTypeSelector(
    selected: MealType,
    onSelect: (MealType) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MealType.entries.forEach { mealType ->
            FlButton(
                text = mealType.label(),
                onClick = { onSelect(mealType) },
                variant =
                    if (selected == mealType) {
                        com.fitlogic.ai.core.designsystem.component.FlButtonVariant.Primary
                    } else {
                        com.fitlogic.ai.core.designsystem.component.FlButtonVariant.Secondary
                    },
            )
        }
    }
}
