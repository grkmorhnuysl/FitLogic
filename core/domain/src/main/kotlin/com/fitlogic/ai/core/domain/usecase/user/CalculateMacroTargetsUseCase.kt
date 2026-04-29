package com.fitlogic.ai.core.domain.usecase.user

import com.fitlogic.ai.core.domain.model.ActivityLevel
import com.fitlogic.ai.core.domain.model.Gender
import com.fitlogic.ai.core.domain.model.GoalType
import com.fitlogic.ai.core.domain.model.MacroTargets
import javax.inject.Inject
import kotlin.math.roundToInt

class CalculateMacroTargetsUseCase
    @Inject
    constructor() {
        operator fun invoke(
            gender: Gender,
            age: Int,
            heightCm: Float,
            weightKg: Float,
            activityLevel: ActivityLevel,
            goalType: GoalType,
        ): MacroTargets {
            val bmr = calculateBmr(gender = gender, age = age, heightCm = heightCm, weightKg = weightKg)
            val activityMultiplier = resolveActivityMultiplier(activityLevel)
            val tdee = bmr * activityMultiplier
            val targetCalories = resolveTargetCalories(tdee = tdee, goalType = goalType)

            val proteinRatio = resolveProteinRatio(goalType)
            val fatRatio = resolveFatRatio(goalType)
            val carbRatio = 1f - proteinRatio - fatRatio

            val proteinGrams = (targetCalories * proteinRatio / 4f).roundToInt()
            val fatGrams = (targetCalories * fatRatio / 9f).roundToInt()
            val carbGrams = (targetCalories * carbRatio / 4f).roundToInt()

            return MacroTargets(
                calories = targetCalories.roundToInt(),
                proteinGrams = proteinGrams,
                carbGrams = carbGrams,
                fatGrams = fatGrams,
            )
        }

        private fun calculateBmr(
            gender: Gender,
            age: Int,
            heightCm: Float,
            weightKg: Float,
        ): Float =
            when (gender) {
                Gender.MALE -> 10 * weightKg + 6.25f * heightCm - 5 * age + 5
                Gender.FEMALE -> 10 * weightKg + 6.25f * heightCm - 5 * age - 161
            }

        private fun resolveActivityMultiplier(activityLevel: ActivityLevel): Float =
            when (activityLevel) {
                ActivityLevel.SEDENTARY -> 1.2f
                ActivityLevel.LIGHT -> 1.375f
                ActivityLevel.MODERATE -> 1.55f
                ActivityLevel.ACTIVE -> 1.725f
                ActivityLevel.ATHLETE -> 1.9f
            }

        private fun resolveTargetCalories(
            tdee: Float,
            goalType: GoalType,
        ): Float =
            when (goalType) {
                GoalType.LOSE_WEIGHT -> (tdee - 400f)
                GoalType.MAINTAIN -> tdee
                GoalType.BUILD_MUSCLE -> (tdee + 300f)
            }.coerceAtLeast(1200f)

        private fun resolveProteinRatio(goalType: GoalType): Float =
            when (goalType) {
                GoalType.LOSE_WEIGHT -> 0.35f
                GoalType.MAINTAIN -> 0.3f
                GoalType.BUILD_MUSCLE -> 0.3f
            }

        private fun resolveFatRatio(goalType: GoalType): Float =
            when (goalType) {
                GoalType.LOSE_WEIGHT -> 0.3f
                GoalType.MAINTAIN -> 0.3f
                GoalType.BUILD_MUSCLE -> 0.25f
            }
    }
