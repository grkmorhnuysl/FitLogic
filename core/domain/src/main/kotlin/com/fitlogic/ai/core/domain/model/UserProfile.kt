package com.fitlogic.ai.core.domain.model

data class UnitPreferences(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val heightUnit: HeightUnit = HeightUnit.CM,
)

data class MacroTargets(
    val calories: Int,
    val proteinGrams: Int,
    val carbGrams: Int,
    val fatGrams: Int,
)

data class UserProfile(
    val id: String,
    val email: String?,
    val displayName: String,
    val age: Int?,
    val heightCm: Float?,
    val weightKg: Float?,
    val gender: Gender?,
    val activityLevel: ActivityLevel?,
    val goalType: GoalType?,
    val macroTargets: MacroTargets?,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val languagePreference: LanguagePreference = LanguagePreference.TR,
    val unitPreferences: UnitPreferences = UnitPreferences(),
    val isGuest: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class OnboardingDraft(
    val displayName: String = "",
    val age: Int? = null,
    val heightCm: Float? = null,
    val weightKg: Float? = null,
    val gender: Gender? = null,
    val activityLevel: ActivityLevel = ActivityLevel.LIGHT,
    val goalType: GoalType = GoalType.MAINTAIN,
)

sealed interface UserAuthState {
    data object SignedOut : UserAuthState

    data class Authenticated(
        val userId: String,
        val email: String,
    ) : UserAuthState

    data object Guest : UserAuthState
}

enum class AppEntryDestination {
    ONBOARDING,
    AUTH,
    HOME,
}
