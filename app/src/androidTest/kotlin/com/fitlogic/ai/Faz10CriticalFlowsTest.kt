package com.fitlogic.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class Faz10CriticalFlowsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun workoutSaveFlow_smoke() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_workout").performClick()
        composeRule.onNodeWithTag("workout_title").assertIsDisplayed()
        if (hasNode("workout_start_empty")) {
            composeRule.onNodeWithTag("workout_start_empty").performClick()
        }
        composeRule.onNodeWithTag("workout_finish").assertExists()
    }

    @Test
    fun nutritionBarcodeFlow_smoke() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_nutrition").performClick()
        composeRule.onNodeWithTag("nutrition_open_barcode_breakfast").performClick()
        composeRule.onNodeWithText("Barkod Tara").assertIsDisplayed()
    }

    @Test
    fun aiInsightFlow_smoke() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_coach").performClick()
        composeRule.onNodeWithTag("coach_title").assertIsDisplayed()
        composeRule.onNodeWithTag("coach_generate_weekly").performClick()
        composeRule.onNodeWithTag("coach_detect_plateau").performClick()
    }

    private fun completeOnboardingAndGuestLogin() {
        composeRule.waitUntil(timeoutMillis = 30_000) {
            hasNode("tab_home") || hasNode("auth_guest") || hasNode("onboarding_next")
        }

        if (hasNode("onboarding_next")) {
            composeRule.onNodeWithTag("onboarding_next").performClick()
            composeRule.onNodeWithTag("onboarding_name").performClick()
            composeRule.onNodeWithTag("onboarding_name").performTextInput("Test User")
            composeRule.onNodeWithTag("onboarding_age").performTextInput("30")
            composeRule.onNodeWithTag("onboarding_height").performTextInput("178")
            composeRule.onNodeWithTag("onboarding_weight").performTextInput("80")
            composeRule.onNodeWithTag("onboarding_gender_male").performClick()
            composeRule.onNodeWithTag("onboarding_next").performClick()
            composeRule.onNodeWithTag("onboarding_activity_moderate").performClick()
            composeRule.onNodeWithTag("onboarding_goal_build").performClick()
            composeRule.onNodeWithTag("onboarding_next").performClick()
            composeRule.onNodeWithTag("onboarding_complete").performClick()
        }
        if (hasNode("auth_guest")) {
            composeRule.onNodeWithTag("auth_guest").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 30_000) {
            hasNode("tab_home") || hasNode("tab_workout") || hasNode("tab_nutrition") || hasNode("tab_coach")
        }
        if (hasNode("tab_home")) {
            composeRule.onNodeWithTag("tab_home").performClick()
        }
    }

    private fun hasNode(tag: String): Boolean =
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
