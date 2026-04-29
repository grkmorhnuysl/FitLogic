@file:Suppress("FunctionName")

package com.fitlogic.ai.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.component.FlCard
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme
import java.time.LocalTime

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToWorkout: () -> Unit = {},
    onNavigateToCoach: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        onSetWeeklyGoal = viewModel::setWeeklyGoal,
        onNavigateToWorkout = onNavigateToWorkout,
        onNavigateToCoach = onNavigateToCoach,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onSetWeeklyGoal: (Int) -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToCoach: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GreetingHeader(modifier = Modifier.testTag("home_title"))
        }

        item {
            HeroCard(
                currentStreak = state.streak.currentDays,
                longestStreak = state.streak.longestDays,
                weeklyCompleted = state.weeklyGoal.completedWorkouts,
                weeklyTarget = state.weeklyGoal.targetWorkouts,
                onStartWorkout = onNavigateToWorkout,
                onSetWeeklyGoal = onSetWeeklyGoal,
            )
        }

        item {
            AiCoachTeaserCard(onTap = onNavigateToCoach)
        }

        item {
            AchievementsCard(
                unlocked = state.achievements.count { it.unlockedAt != null },
                total = state.achievements.size,
            )
        }

        item {
            AnimatedVisibility(
                visible = state.latestUnlockedAchievement != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FlCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🏆 Yeni rozet açıldı!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Text(
                            text = state.latestUnlockedAchievement?.title.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader(modifier: Modifier = Modifier) {
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> "Günaydın"
            in 12..17 -> "İyi günler"
            else -> "İyi akşamlar"
        }
    }
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$greeting 👋",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Bugün ne yapıyoruz?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HeroCard(
    currentStreak: Int,
    longestStreak: Int,
    weeklyCompleted: Int,
    weeklyTarget: Int,
    onStartWorkout: () -> Unit,
    onSetWeeklyGoal: (Int) -> Unit,
) {
    val weeklyProgress = if (weeklyTarget > 0) (weeklyCompleted.toFloat() / weeklyTarget).coerceIn(0f, 1f) else 0f

    FlCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🔥",
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "günlük seri  ·  en uzun: $longestStreak gün",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Haftalık hedef",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$weeklyCompleted / $weeklyTarget antrenman",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                LinearProgressIndicator(
                    progress = { weeklyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Hedef:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    listOf(3, 4, 5).forEach { n ->
                        val isSelected = n == weeklyTarget
                        TextButton(
                            onClick = { onSetWeeklyGoal(n) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Text(
                                text = "$n",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = "Antrenmanı Başlat  →",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun AiCoachTeaserCard(onTap: () -> Unit) {
    FlCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "🤖", style = MaterialTheme.typography.headlineSmall)
                Column {
                    Text(
                        text = "AI Koç",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "Önerilerin seni bekliyor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun AchievementsCard(unlocked: Int, total: Int) {
    FlCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "🏅", style = MaterialTheme.typography.titleLarge)
                    Text(text = "Rozetler", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = if (total > 0) "%${(unlocked * 100 / total)}" else "—",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            LinearProgressIndicator(
                progress = { if (total > 0) unlocked.toFloat() / total else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraLarge),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = "$unlocked / $total rozet açık",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FitLogicTheme {
        HomeContent(
            state = HomeUiState(isLoading = false),
            onSetWeeklyGoal = {},
            onNavigateToWorkout = {},
            onNavigateToCoach = {},
        )
    }
}
