@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.exercises

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.fitlogic.ai.core.designsystem.component.FlButton
import com.fitlogic.ai.core.designsystem.component.FlCard
import com.fitlogic.ai.core.designsystem.component.FlLoadingIndicator
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem

private val tabLabels = listOf("Aciklama", "Yaygin Hatalar", "Alternatifler")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    onExerciseClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ExercisesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseDetail(exerciseId)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.selectedExercise?.name ?: "Egzersiz Detay") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
        bottomBar = {
            if (state.hasActiveWorkout) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    FlButton(
                        text = if (state.isAddingToWorkout) "Ekleniyor..." else "Bu egzersizi ekle",
                        onClick = { viewModel.addToWorkout(exerciseId) },
                        enabled = !state.isAddingToWorkout,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading -> FlLoadingIndicator(modifier = Modifier.fillMaxSize())
            state.selectedExercise == null ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Egzersiz yuklenemedi.")
                }
            else ->
                DetailContent(
                    exercise = state.selectedExercise!!,
                    alternatives = state.alternatives,
                    selectedTab = state.selectedTab,
                    onTabSelect = viewModel::selectTab,
                    onExerciseClick = onExerciseClick,
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun DetailContent(
    exercise: ExerciseCatalogItem,
    alternatives: List<ExerciseCatalogItem>,
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    onExerciseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ExerciseGifArea(exercise = exercise)

        TabRow(selectedTabIndex = selectedTab) {
            tabLabels.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelect(index) },
                    text = { Text(label) },
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (selectedTab) {
                0 -> InstructionsTab(exercise)
                1 -> MistakesTab(exercise)
                2 -> AlternativesTab(alternatives, onExerciseClick)
            }
        }
    }
}

@Composable
private fun ExerciseGifArea(exercise: ExerciseCatalogItem) {
    if (exercise.gifAssetPath.isNotEmpty()) {
        SubcomposeAsyncImage(
            model = exercise.gifAssetPath,
            contentDescription = exercise.name,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(220.dp),
        ) {
            when (painter.state) {
                is coil.compose.AsyncImagePainter.State.Error,
                is coil.compose.AsyncImagePainter.State.Empty,
                -> ExercisePlaceholder(exercise.name)
                else -> SubcomposeAsyncImageContent()
            }
        }
    } else {
        ExercisePlaceholder(exercise.name)
    }
}

@Composable
private fun ExercisePlaceholder(exerciseName: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = exerciseName.take(1),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun InstructionsTab(exercise: ExerciseCatalogItem) {
    if (exercise.instructionSteps.isNotEmpty()) {
        exercise.instructionSteps.forEachIndexed { index, step ->
            Text(
                text = "${index + 1}. $step",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    } else {
        Text(
            text = exercise.instructions.ifEmpty { "Aciklama mevcut degil." },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MistakesTab(exercise: ExerciseCatalogItem) {
    if (exercise.commonMistakes.isNotEmpty()) {
        exercise.commonMistakes.forEach { mistake ->
            Text(
                text = "• $mistake",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    } else {
        Text(
            text = "Yaygin hata bilgisi mevcut degil.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AlternativesTab(
    alternatives: List<ExerciseCatalogItem>,
    onExerciseClick: (String) -> Unit,
) {
    if (alternatives.isNotEmpty()) {
        alternatives.forEach { alt ->
            FlCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onExerciseClick(alt.id) },
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(alt.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${alt.muscleGroup} · ${alt.equipment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    } else {
        Text(
            text = "Alternatif egzersiz bulunamadi.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
