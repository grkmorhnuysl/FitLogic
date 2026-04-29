package com.fitlogic.ai.feature.onboarding

import com.fitlogic.ai.core.domain.model.ActivityLevel
import com.fitlogic.ai.core.domain.model.Gender
import com.fitlogic.ai.core.domain.model.GoalType
import com.fitlogic.ai.core.domain.model.MacroTargets

enum class OnboardingStep {
    WELCOME,
    BASIC_INFO,
    GOAL,
    SUMMARY,
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val displayName: String = "",
    val age: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.LIGHT,
    val goalType: GoalType = GoalType.MAINTAIN,
    val suggestion: MacroTargets? = null,
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val message: String? = null,
)
