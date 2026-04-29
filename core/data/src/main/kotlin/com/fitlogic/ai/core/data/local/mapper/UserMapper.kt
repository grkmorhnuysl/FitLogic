package com.fitlogic.ai.core.data.local.mapper

import com.fitlogic.ai.core.data.local.entity.UserEntity
import com.fitlogic.ai.core.domain.model.ActivityLevel
import com.fitlogic.ai.core.domain.model.Gender
import com.fitlogic.ai.core.domain.model.GoalType
import com.fitlogic.ai.core.domain.model.HeightUnit
import com.fitlogic.ai.core.domain.model.LanguagePreference
import com.fitlogic.ai.core.domain.model.MacroTargets
import com.fitlogic.ai.core.domain.model.ThemePreference
import com.fitlogic.ai.core.domain.model.UnitPreferences
import com.fitlogic.ai.core.domain.model.UserProfile
import com.fitlogic.ai.core.domain.model.WeightUnit

fun UserEntity.toDomain(onboardingCompleted: Boolean): UserProfile =
    UserProfile(
        id = id,
        email = email,
        displayName = displayName,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        gender = gender?.let { enumValueOfOrNull<Gender>(it) },
        activityLevel = activityLevel?.let { enumValueOfOrNull<ActivityLevel>(it) },
        goalType = goalType?.let { enumValueOfOrNull<GoalType>(it) },
        macroTargets = toMacroTargetsOrNull(),
        themePreference = enumValueOfOrNull<ThemePreference>(theme) ?: ThemePreference.SYSTEM,
        languagePreference = enumValueOfOrNull<LanguagePreference>(language) ?: LanguagePreference.TR,
        unitPreferences =
            UnitPreferences(
                weightUnit = enumValueOfOrNull<WeightUnit>(weightUnit) ?: WeightUnit.KG,
                heightUnit = enumValueOfOrNull<HeightUnit>(heightUnit) ?: HeightUnit.CM,
            ),
        isGuest = isGuest,
        onboardingCompleted = onboardingCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun UserProfile.toEntity(): UserEntity =
    UserEntity(
        id = id,
        email = email,
        displayName = displayName,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        gender = gender?.name,
        activityLevel = activityLevel?.name,
        goalType = goalType?.name,
        targetCalories = macroTargets?.calories,
        proteinGrams = macroTargets?.proteinGrams,
        carbGrams = macroTargets?.carbGrams,
        fatGrams = macroTargets?.fatGrams,
        theme = themePreference.name,
        language = languagePreference.name,
        weightUnit = unitPreferences.weightUnit.name,
        heightUnit = unitPreferences.heightUnit.name,
        isGuest = isGuest,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis(),
    )

private fun UserEntity.toMacroTargetsOrNull(): MacroTargets? {
    val hasAllMacroValues =
        listOf(targetCalories, proteinGrams, carbGrams, fatGrams).all { it != null }
    return if (hasAllMacroValues) {
        MacroTargets(
            calories = targetCalories!!,
            proteinGrams = proteinGrams!!,
            carbGrams = carbGrams!!,
            fatGrams = fatGrams!!,
        )
    } else {
        null
    }
}

private inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? {
    return runCatching { enumValueOf<T>(name) }.getOrNull()
}
