@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.coach

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlCard

@Composable
fun CoachScreen(
    modifier: Modifier = Modifier,
    viewModel: CoachViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.message) {
        if (state.message != null) viewModel.clearMessage()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("AI Koc", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("coach_title"))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::generateWeeklyReport, modifier = Modifier.testTag("coach_generate_weekly")) {
                    Text("Haftalik Rapor")
                }
                Button(onClick = viewModel::detectPlateau, modifier = Modifier.testTag("coach_detect_plateau")) {
                    Text("Plato Kontrol")
                }
            }
        }
        item {
            state.selectedInsight?.let { selected ->
                FlCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Insight Detay", style = MaterialTheme.typography.titleMedium)
                        Text(selected.title, style = MaterialTheme.typography.titleSmall)
                        Text(selected.body)
                    }
                }
            }
        }
        items(state.insights) { insight ->
            FlCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(insight.title, style = MaterialTheme.typography.titleMedium)
                    Text(insight.body.take(140))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.selectInsight(insight.id) }) {
                            Text("Detay")
                        }
                        Button(onClick = { viewModel.markAsRead(insight.id) }) {
                            Text("Okundu")
                        }
                    }
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
