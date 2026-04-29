@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlCard
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutScreen(
    modifier: Modifier = Modifier,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    WorkoutContent(
        state = state,
        modifier = modifier,
        onStartEmpty = viewModel::startEmptyWorkout,
        onStartTemplate = { viewModel.startFromTemplate("Genel") },
        onStartFromHistory = viewModel::startFromHistory,
        onExerciseQueryChange = viewModel::onExerciseQueryChange,
        onAddExercise = viewModel::addExercise,
        onSaveSet = viewModel::saveSet,
        onCopyLastSet = viewModel::copyLastSet,
        onFinishWorkout = viewModel::finishWorkout,
        onOpenDetail = viewModel::selectHistoryWorkout,
        onCloseDetail = viewModel::clearSelectedWorkout,
        onClearMessage = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WorkoutContent(
    state: WorkoutUiState,
    onStartEmpty: () -> Unit,
    onStartTemplate: () -> Unit,
    onStartFromHistory: (String) -> Unit,
    onExerciseQueryChange: (String) -> Unit,
    onAddExercise: (String) -> Unit,
    onSaveSet: (String, String, String) -> Unit,
    onCopyLastSet: (String) -> Unit,
    onFinishWorkout: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onCloseDetail: () -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showTimerSheet by rememberSaveable { mutableStateOf(false) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(0) }
    var timerRunning by rememberSaveable { mutableStateOf(false) }
    val weightInputs = remember(state.activeSession?.workout?.id) { mutableStateMapOf<String, String>() }
    val repsInputs = remember(state.activeSession?.workout?.id) { mutableStateMapOf<String, String>() }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onClearMessage()
    }

    LaunchedEffect(timerRunning, remainingSeconds) {
        if (!timerRunning || remainingSeconds <= 0) return@LaunchedEffect
        delay(1000)
        remainingSeconds -= 1
        if (remainingSeconds <= 0) timerRunning = false
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Antrenman", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("workout_title"))
        SnackbarHost(hostState = snackbarHostState)

        if (state.isLoading) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.padding(top = 2.dp))
                Text("Islem suruyor...")
            }
        }

        ActiveWorkoutSection(
            state = state,
            weightInputs = weightInputs,
            repsInputs = repsInputs,
            onStartEmpty = onStartEmpty,
            onStartTemplate = onStartTemplate,
            onStartFromHistory = onStartFromHistory,
            onExerciseQueryChange = onExerciseQueryChange,
            onAddExercise = onAddExercise,
            onSaveSet = onSaveSet,
            onCopyLastSet = onCopyLastSet,
            onFinishWorkout = onFinishWorkout,
            onOpenTimer = { showTimerSheet = true },
        )

        FinishedSummaryCard(summary = state.lastFinishedSummary)

        HistorySection(
            history = state.history,
            onOpenDetail = onOpenDetail,
            onRepeatWorkout = onStartFromHistory,
        )
    }

    if (showTimerSheet) {
        ModalBottomSheet(onDismissRequest = { showTimerSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Dinlenme Timer", style = MaterialTheme.typography.titleMedium)
                Text(if (remainingSeconds > 0) "Kalan: ${remainingSeconds}s" else "Bir sure secip baslatin.")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(60, 90, 120).forEach { duration ->
                        Button(onClick = {
                            remainingSeconds = duration
                            timerRunning = true
                        }) {
                            Text("${duration}s")
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { timerRunning = !timerRunning }, enabled = remainingSeconds > 0) {
                        Text(if (timerRunning) "Duraklat" else "Devam Et")
                    }
                    TextButton(onClick = {
                        remainingSeconds = 0
                        timerRunning = false
                    }) {
                        Text("Sifirla")
                    }
                }
            }
        }
    }

    state.selectedWorkoutDetail?.let {
        WorkoutDetailDialog(detail = it, onDismiss = onCloseDetail)
    }
}

@Composable
private fun ActiveWorkoutSection(
    state: WorkoutUiState,
    weightInputs: MutableMap<String, String>,
    repsInputs: MutableMap<String, String>,
    onStartEmpty: () -> Unit,
    onStartTemplate: () -> Unit,
    onStartFromHistory: (String) -> Unit,
    onExerciseQueryChange: (String) -> Unit,
    onAddExercise: (String) -> Unit,
    onSaveSet: (String, String, String) -> Unit,
    onCopyLastSet: (String) -> Unit,
    onFinishWorkout: () -> Unit,
    onOpenTimer: () -> Unit,
) {
    FlCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            val active = state.activeSession
            if (active == null) {
                Text("Aktif antrenman yok", style = MaterialTheme.typography.titleMedium)
                Text("Bos, sablon veya gecmisten tekrar ile baslayabilirsin.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onStartEmpty, modifier = Modifier.testTag("workout_start_empty")) { Text("Bos Baslat") }
                    Button(onClick = onStartTemplate) { Text("Sablon") }
                    Button(
                        onClick = { state.history.firstOrNull()?.workoutId?.let(onStartFromHistory) },
                        enabled = state.history.isNotEmpty(),
                    ) {
                        Text("Gecmisi Tekrarla")
                    }
                }
                return@Column
            }

            Text(active.workout.title, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenTimer, modifier = Modifier.testTag("workout_rest_timer")) { Text("Dinlenme Timer") }
                Button(onClick = onFinishWorkout, modifier = Modifier.testTag("workout_finish")) { Text("Antrenmani Bitir") }
            }

            OutlinedTextField(
                value = state.exerciseQuery,
                onValueChange = onExerciseQueryChange,
                label = { Text("Egzersiz ara") },
                modifier = Modifier.fillMaxWidth(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                state.exerciseResults.take(6).forEach { exercise ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(exercise.name, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onAddExercise(exercise.id) }) { Text("Ekle") }
                    }
                }
            }

            active.exercises.forEach { exerciseWithSets ->
                ExerciseBlock(
                    exerciseWithSets = exerciseWithSets,
                    weight = weightInputs[exerciseWithSets.exercise.id] ?: "",
                    reps = repsInputs[exerciseWithSets.exercise.id] ?: "",
                    onWeightChange = { weightInputs[exerciseWithSets.exercise.id] = it },
                    onRepsChange = { repsInputs[exerciseWithSets.exercise.id] = it },
                    onSaveSet = {
                        onSaveSet(
                            exerciseWithSets.exercise.id,
                            weightInputs[exerciseWithSets.exercise.id] ?: "",
                            repsInputs[exerciseWithSets.exercise.id] ?: "",
                        )
                    },
                    onCopyLastSet = { onCopyLastSet(exerciseWithSets.exercise.id) },
                )
            }
        }
    }
}

@Composable
private fun ExerciseBlock(
    exerciseWithSets: com.fitlogic.ai.core.domain.model.WorkoutExerciseWithSets,
    weight: String,
    reps: String,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onSaveSet: () -> Unit,
    onCopyLastSet: () -> Unit,
) {
    FlCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = exerciseWithSets.exercise.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            exerciseWithSets.previousReference?.let { ref ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Son",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${ref.weightKg} kg  ×  ${ref.reps} rep",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ağırlık (kg)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                QuickAdjustRow(
                    deltas = listOf(-5f, -2.5f, 2.5f, 5f),
                    current = weight,
                    onAdjust = onWeightChange,
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = onWeightChange,
                    label = { Text("kg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tekrar",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                QuickAdjustRow(
                    deltas = listOf(-2f, -1f, 1f, 2f),
                    current = reps,
                    onAdjust = onRepsChange,
                    isInteger = true,
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = onRepsChange,
                    label = { Text("rep") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            Button(
                onClick = onSaveSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("workout_save_set"),
            ) {
                Text(
                    text = "✓  Set Kaydet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            TextButton(
                onClick = onCopyLastSet,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                Text(
                    text = "Son Seti Kopyala",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (exerciseWithSets.sets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    exerciseWithSets.sets.forEachIndexed { index, set ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Set ${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${set.weightKg} kg  ×  ${set.reps}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            if (set.isPr) {
                                Text(
                                    text = "PR 🏆",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAdjustRow(
    deltas: List<Float>,
    current: String,
    onAdjust: (String) -> Unit,
    isInteger: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        deltas.forEach { delta ->
            val label = if (delta % 1f == 0f) {
                if (delta > 0) "+${delta.toInt()}" else "${delta.toInt()}"
            } else {
                if (delta > 0) "+$delta" else "$delta"
            }
            OutlinedButton(
                onClick = {
                    val adjusted = (current.toFloatOrNull() ?: 0f) + delta
                    val clamped = adjusted.coerceAtLeast(0f)
                    onAdjust(
                        if (isInteger || clamped % 1f == 0f) {
                            clamped.toInt().toString()
                        } else {
                            String.format(java.util.Locale.US, "%.1f", clamped)
                        },
                    )
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun FinishedSummaryCard(summary: com.fitlogic.ai.core.domain.model.FinishedWorkoutSummary?) {
    if (summary == null) return
    val hasPr = summary.prCount > 0
    val pulse =
        rememberInfiniteTransition(label = "pr_pulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(animation = tween(650), repeatMode = RepeatMode.Reverse),
            label = "pr_scale",
        )
    FlCard {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Son Antrenman Ozeti", style = MaterialTheme.typography.titleMedium)
            Text("Toplam hacim: ${summary.totalVolume}")
            Text("Toplam set: ${summary.totalSets}")
            Text("Sure: ${summary.durationMinutes} dk")
            Text("PR sayisi: ${summary.prCount}")
            AnimatedVisibility(visible = hasPr, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = "Tebrikler! Yeni PR acildi!",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.scale(pulse.value).testTag("workout_pr_celebration"),
                )
            }
        }
    }
}

@Composable
private fun HistorySection(
    history: List<com.fitlogic.ai.core.domain.model.WorkoutHistoryEntry>,
    onOpenDetail: (String) -> Unit,
    onRepeatWorkout: (String) -> Unit,
) {
    FlCard {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Antrenman Gecmisi", style = MaterialTheme.typography.titleMedium)
            if (history.isEmpty()) {
                Text("Henuz tamamlanan antrenman yok.")
                return@Column
            }

            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val grouped = history.groupBy { Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate() }
            grouped.toSortedMap(compareByDescending { it }).forEach { (date, entries) ->
                Text("Takvim: ${date.format(formatter)}", style = MaterialTheme.typography.labelLarge)
                entries.forEach { item ->
                    FlCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title)
                                Text("Hacim ${item.totalVolume} | Set ${item.totalSets}")
                            }
                            Column {
                                TextButton(onClick = { onOpenDetail(item.workoutId) }) { Text("Detay") }
                                TextButton(onClick = { onRepeatWorkout(item.workoutId) }) { Text("Tekrarla") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDetailDialog(
    detail: com.fitlogic.ai.core.domain.model.WorkoutDetail,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } },
        title = { Text(detail.workout.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.exercises.forEach { exercise ->
                    Text(exercise.exercise.exerciseName, style = MaterialTheme.typography.labelLarge)
                    exercise.sets.forEachIndexed { index, set ->
                        Text("Set ${index + 1}: ${set.weightKg}kg x ${set.reps} ${if (set.isPr) "(PR)" else ""}")
                    }
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkoutScreenPreview() {
    FitLogicTheme {
        WorkoutContent(
            state = WorkoutUiState(),
            onStartEmpty = {},
            onStartTemplate = {},
            onStartFromHistory = {},
            onExerciseQueryChange = {},
            onAddExercise = {},
            onSaveSet = { _, _, _ -> },
            onCopyLastSet = {},
            onFinishWorkout = {},
            onOpenDetail = {},
            onCloseDetail = {},
            onClearMessage = {},
        )
    }
}
