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
import com.fitlogic.ai.core.designsystem.component.FlButtonVariant
import com.fitlogic.ai.core.designsystem.component.FlTextField
import com.fitlogic.ai.core.domain.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun ManualAddFoodScreen(
    targetMealType: MealType,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManualAddFoodViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(targetMealType) {
        viewModel.setMealType(targetMealType)
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
                title = { Text("Manuel Yemek Ekle") },
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
                    .padding(innerPadding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = "Yemek adi",
                modifier = Modifier.fillMaxWidth(),
            )
            FlTextField(
                value = state.gramsText,
                onValueChange = viewModel::onGramsChange,
                label = "Gram",
                modifier = Modifier.fillMaxWidth(),
            )
            FlTextField(
                value = state.kcalText,
                onValueChange = viewModel::onKcalChange,
                label = "Kalori (opsiyonel)",
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlTextField(
                    value = state.proteinText,
                    onValueChange = viewModel::onProteinChange,
                    label = "Protein",
                    modifier = Modifier.weight(1f),
                )
                FlTextField(
                    value = state.carbText,
                    onValueChange = viewModel::onCarbChange,
                    label = "Karb",
                    modifier = Modifier.weight(1f),
                )
                FlTextField(
                    value = state.fatText,
                    onValueChange = viewModel::onFatChange,
                    label = "Yag",
                    modifier = Modifier.weight(1f),
                )
            }
            Text("Bu akista kayit, katalogdaki en yakin ad eslesmesiyle eklenir.")
            FlButton(
                text = if (state.isLoading) "Kaydediliyor..." else "Kaydet",
                onClick = viewModel::save,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            FlButton(
                text = "Geri Don",
                onClick = onBack,
                variant = FlButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
