@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlCard

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var weightInput by remember { mutableStateOf("") }

    LaunchedEffect(state.message) {
        if (state.message != null) viewModel.clearMessage()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Istatistik", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Haftalik Ozet", style = MaterialTheme.typography.titleMedium)
                    Text("Tamamlanan antrenman: ${state.summary.workoutsCompleted}")
                    Text("Toplam hacim: ${state.summary.totalVolume.toInt()} kg")
                    Text("PR sayisi: ${state.summary.prCount}")
                    Text("Ort. sure: ${state.summary.avgWorkoutMinutes} dk")
                }
            }
        }
        item {
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Vucut Agirligi Takibi", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Kilo (kg)") },
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = { viewModel.addWeight(weightInput); weightInput = "" }) {
                            Text("Ekle")
                        }
                    }
                    Text("Son kayitlar")
                    state.weightTrend.takeLast(7).forEach {
                        Text("- ${it.weightKg} kg")
                    }
                }
            }
        }
        item {
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Egzersiz Ilerlemesi", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.selectedExerciseId,
                        onValueChange = viewModel::onExerciseIdChanged,
                        label = { Text("Exercise ID") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.exerciseProgress.takeLast(5).forEach {
                        Text("- Hacim: ${it.totalVolume.toInt()} | En iyi: ${it.bestWeightKg} kg")
                    }
                }
            }
        }
        item {
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Haftalik Hacim", style = MaterialTheme.typography.titleMedium)
                    state.weeklyVolume.forEach {
                        Text("- ${it.weekLabel}: ${it.totalVolume.toInt()} kg")
                    }
                }
            }
        }
        item {
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kas Grubu Dagilimi", style = MaterialTheme.typography.titleMedium)
                    state.muscleDistribution.forEach {
                        Text("- ${it.muscleGroup}: ${it.totalVolume.toInt()} kg")
                    }
                }
            }
        }
        item {
            Text("PR Listesi", style = MaterialTheme.typography.titleMedium)
        }
        items(state.prHistory) {
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(it.exerciseName, style = MaterialTheme.typography.titleSmall)
                    Text("${it.weightKg} kg x ${it.reps} | Hacim ${it.volume.toInt()}")
                }
            }
        }
        state.message?.let { message ->
            item {
                Text(message)
            }
        }
    }
}
