@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlCard
import com.fitlogic.ai.core.designsystem.component.FlEmptyState
import com.fitlogic.ai.core.designsystem.component.FlLoadingIndicator
import com.fitlogic.ai.core.designsystem.component.FlTextField
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem

@Composable
fun ExercisesScreen(
    onExerciseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExercisesViewModel = hiltViewModel(),
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
        ExercisesContent(
            state = state,
            onQueryChange = viewModel::onQueryChange,
            onToggleMuscleGroup = viewModel::toggleMuscleGroup,
            onToggleEquipment = viewModel::toggleEquipment,
            onSetDifficulty = viewModel::setDifficulty,
            onExerciseClick = onExerciseClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun ExercisesContent(
    state: ExercisesUiState,
    onQueryChange: (String) -> Unit,
    onToggleMuscleGroup: (String) -> Unit,
    onToggleEquipment: (String) -> Unit,
    onSetDifficulty: (String?) -> Unit,
    onExerciseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        FlTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = "Egzersiz ara...",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(MuscleGroups) { group ->
                FilterChip(
                    selected = group in state.selectedMuscleGroups,
                    onClick = { onToggleMuscleGroup(group) },
                    label = { Text(group) },
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(EquipmentTypes) { equipment ->
                FilterChip(
                    selected = equipment in state.selectedEquipments,
                    onClick = { onToggleEquipment(equipment) },
                    label = { Text(equipment) },
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DifficultyLevels.forEach { level ->
                FilterChip(
                    selected = state.selectedDifficulty == level,
                    onClick = { onSetDifficulty(level) },
                    label = { Text(level) },
                )
            }
        }

        when {
            state.isLoading -> FlLoadingIndicator(modifier = Modifier.fillMaxSize())
            state.results.isEmpty() ->
                FlEmptyState(
                    title = "Egzersiz bulunamadi",
                    description = "Arama kriterlerinizi degistirmeyi deneyin.",
                    modifier = Modifier.fillMaxSize(),
                )
            else ->
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.results, key = { it.id }) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onClick = { onExerciseClick(exercise.id) },
                        )
                    }
                }
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: ExerciseCatalogItem,
    onClick: () -> Unit,
) {
    FlCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExerciseBadge(exercise.muscleGroup)
                ExerciseBadge(exercise.equipment)
                ExerciseBadge(exercise.difficulty)
            }
        }
    }
}

@Composable
private fun ExerciseBadge(text: String) {
    SuggestionChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
    )
}
