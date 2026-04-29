@file:Suppress("FunctionName", "UnusedPrivateMember")

package com.fitlogic.ai.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme
import com.fitlogic.ai.core.domain.model.ActivityLevel
import com.fitlogic.ai.core.domain.model.Gender
import com.fitlogic.ai.core.domain.model.GoalType

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onCompleted()
    }
    OnboardingContent(
        state = state,
        modifier = modifier,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onAgeChange = viewModel::onAgeChange,
        onHeightChange = viewModel::onHeightChange,
        onWeightChange = viewModel::onWeightChange,
        onGenderChange = viewModel::onGenderChange,
        onActivityLevelChange = viewModel::onActivityLevelChange,
        onGoalTypeChange = viewModel::onGoalTypeChange,
        onNext = viewModel::nextStep,
        onBack = viewModel::previousStep,
        onComplete = viewModel::complete,
    )
}

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun OnboardingContent(
    state: OnboardingUiState,
    onDisplayNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onActivityLevelChange: (ActivityLevel) -> Unit,
    onGoalTypeChange: (GoalType) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Onboarding", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        when (state.step) {
            OnboardingStep.WELCOME -> {
                Text(
                    text = "FitLogic'a hos geldin. Kisa bir kurulum ile baslayalim.",
                    modifier = Modifier.testTag("onboarding_welcome"),
                )
            }
            OnboardingStep.BASIC_INFO -> {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = onDisplayNameChange,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name"),
                    label = { Text("Adin") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.age,
                    onValueChange = onAgeChange,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_age"),
                    label = { Text("Yas") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.heightCm,
                    onValueChange = onHeightChange,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_height"),
                    label = { Text("Boy (cm)") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.weightKg,
                    onValueChange = onWeightChange,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_weight"),
                    label = { Text("Kilo (kg)") },
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = { onGenderChange(Gender.MALE) }, modifier = Modifier.testTag("onboarding_gender_male")) { Text("Erkek") }
                    TextButton(onClick = { onGenderChange(Gender.FEMALE) }, modifier = Modifier.testTag("onboarding_gender_female")) { Text("Kadin") }
                }
            }
            OnboardingStep.GOAL -> {
                Text("Aktivite seviyesi:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onActivityLevelChange(ActivityLevel.LIGHT) }, modifier = Modifier.testTag("onboarding_activity_light")) { Text("Hafif") }
                    TextButton(onClick = { onActivityLevelChange(ActivityLevel.MODERATE) }, modifier = Modifier.testTag("onboarding_activity_moderate")) { Text("Orta") }
                    TextButton(onClick = { onActivityLevelChange(ActivityLevel.ACTIVE) }, modifier = Modifier.testTag("onboarding_activity_active")) { Text("Aktif") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hedef:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onGoalTypeChange(GoalType.LOSE_WEIGHT) }, modifier = Modifier.testTag("onboarding_goal_lose")) { Text("Kilo ver") }
                    TextButton(onClick = { onGoalTypeChange(GoalType.MAINTAIN) }, modifier = Modifier.testTag("onboarding_goal_maintain")) { Text("Koru") }
                    TextButton(onClick = { onGoalTypeChange(GoalType.BUILD_MUSCLE) }, modifier = Modifier.testTag("onboarding_goal_build")) { Text("Kas yap") }
                }
            }
            OnboardingStep.SUMMARY -> {
                Text("Ilk oneriniz hazir.")
                Text("Kalori: ${state.suggestion?.calories ?: "-"} kcal")
                Text("Protein: ${state.suggestion?.proteinGrams ?: "-"} g")
                Text("Karbonhidrat: ${state.suggestion?.carbGrams ?: "-"} g")
                Text("Yag: ${state.suggestion?.fatGrams ?: "-"} g")
            }
        }

        if (!state.message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.testTag("onboarding_back")) { Text("Geri") }
            if (state.step == OnboardingStep.SUMMARY) {
                Button(onClick = onComplete, enabled = !state.isLoading, modifier = Modifier.testTag("onboarding_complete")) {
                    if (state.isLoading) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text("Tamamla")
                    }
                }
            } else {
                Button(onClick = onNext, modifier = Modifier.testTag("onboarding_next")) { Text("Ileri") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPreview() {
    FitLogicTheme {
        OnboardingContent(
            state = OnboardingUiState(),
            onDisplayNameChange = {},
            onAgeChange = {},
            onHeightChange = {},
            onWeightChange = {},
            onGenderChange = {},
            onActivityLevelChange = {},
            onGoalTypeChange = {},
            onNext = {},
            onBack = {},
            onComplete = {},
        )
    }
}
