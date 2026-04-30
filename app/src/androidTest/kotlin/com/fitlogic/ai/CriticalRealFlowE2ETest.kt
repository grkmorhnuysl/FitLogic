package com.fitlogic.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class CriticalRealFlowE2ETest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun workoutFlow_shouldPersistSetAndShowHistory() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_workout").performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            hasNode("workout_start_template") || hasNode("workout_finish")
        }
        if (!hasNode("workout_start_template") && hasNode("workout_finish")) {
            composeRule.onNodeWithTag("workout_finish").performClick()
            composeRule.waitUntil(timeoutMillis = 20_000) {
                hasNode("workout_start_template")
            }
        }
        composeRule.onNodeWithTag("workout_start_template").performClick()

        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("workout_weight_input").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag("workout_weight_input").onFirst().performTextInput("40")
        composeRule.onAllNodesWithTag("workout_reps_input").onFirst().performTextInput("10")
        composeRule.onAllNodesWithTag("workout_save_set").onFirst().performClick()

        composeRule.waitUntil(timeoutMillis = 20_000) {
            hasNode("workout_set_row_1")
        }
        composeRule.onNodeWithTag("workout_finish").performClick()
        composeRule.onNodeWithTag("workout_history_item").assertIsDisplayed()
    }

    @Test
    fun nutritionFlow_shouldSaveFoodEntryToMealList() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_nutrition").performClick()
        composeRule.onNodeWithTag("nutrition_add_food_breakfast").performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("nutrition_search_input", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("nutrition_search_input", useUnmergedTree = true).performTextInput("a")
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("nutrition_search_item").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag("nutrition_search_item").onFirst().performClick()

        composeRule.onNodeWithTag("nutrition_grams_input").performTextClearance()
        composeRule.onNodeWithTag("nutrition_grams_input").performTextInput("120")
        composeRule.onNodeWithTag("nutrition_save_food").performClick()

        composeRule.waitUntil(timeoutMillis = 20_000) {
            hasNode("tab_nutrition")
        }
        composeRule.onNodeWithTag("tab_nutrition").performClick()
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("nutrition_entry_breakfast").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("nutrition_entry_breakfast").assertIsDisplayed()
    }

    @Test
    fun coachFlow_shouldSendChatAndReceiveResponse() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_coach").performClick()
        composeRule.onNodeWithTag("coach_chat_input").performTextInput("Bugun ne yapayim?")
        composeRule.onNodeWithTag("coach_chat_send").performClick()

        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule.onAllNodesWithTag("coach_chat_ai_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("coach_chat_ai_message").assertIsDisplayed()
    }

    private fun completeOnboardingAndGuestLogin() {
        composeRule.waitUntil(timeoutMillis = 30_000) {
            hasNode("tab_home") || hasNode("auth_guest") || hasNode("onboarding_next")
        }

        if (hasNode("onboarding_next")) {
            composeRule.onNodeWithTag("onboarding_next").performClick()
            composeRule.onNodeWithTag("onboarding_name").performClick()
            composeRule.onNodeWithTag("onboarding_name").performTextInput("Critical User")
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
            hasNode("tab_home") && hasNode("tab_workout") && hasNode("tab_nutrition") && hasNode("tab_coach")
        }
    }

    private fun hasNode(tag: String): Boolean =
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
