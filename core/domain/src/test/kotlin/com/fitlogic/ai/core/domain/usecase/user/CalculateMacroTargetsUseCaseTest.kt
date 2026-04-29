package com.fitlogic.ai.core.domain.usecase.user

import com.fitlogic.ai.core.domain.model.ActivityLevel
import com.fitlogic.ai.core.domain.model.Gender
import com.fitlogic.ai.core.domain.model.GoalType
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateMacroTargetsUseCaseTest {
    private val useCase = CalculateMacroTargetsUseCase()

    @Test
    fun `should return valid targets for male lose weight`() {
        val result =
            useCase(
                gender = Gender.MALE,
                age = 29,
                heightCm = 178f,
                weightKg = 85f,
                activityLevel = ActivityLevel.MODERATE,
                goalType = GoalType.LOSE_WEIGHT,
            )

        assertTrue(result.calories > 1200)
        assertTrue(result.proteinGrams > 0)
        assertTrue(result.carbGrams > 0)
        assertTrue(result.fatGrams > 0)
    }

    @Test
    fun `should return valid targets for female build muscle`() {
        val result =
            useCase(
                gender = Gender.FEMALE,
                age = 24,
                heightCm = 165f,
                weightKg = 58f,
                activityLevel = ActivityLevel.ACTIVE,
                goalType = GoalType.BUILD_MUSCLE,
            )

        assertTrue(result.calories > 1200)
        assertTrue(result.proteinGrams > 0)
        assertTrue(result.carbGrams > 0)
        assertTrue(result.fatGrams > 0)
    }
}
