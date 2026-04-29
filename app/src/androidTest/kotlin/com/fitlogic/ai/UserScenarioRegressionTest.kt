package com.fitlogic.ai

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class UserScenarioRegressionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun profileNotificationAndSyncFlow_shouldStayResponsive() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.onNodeWithTag("profile_notification_workout").performClick()
        composeRule.onNodeWithTag("profile_notification_water").performClick()
        composeRule.onNodeWithTag("profile_notification_weekly").performClick()
        composeRule.onNodeWithTag("profile_sync_now").performClick()
        composeRule.onNodeWithTag("tab_home").performClick()
        composeRule.onNodeWithTag("home_title").assertIsDisplayed()
    }

    @Test
    fun deleteAccountConfirmationFlow_shouldBlockThenAllow() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.onNodeWithTag("profile_delete_request").performClick()
        composeRule.onNodeWithTag("profile_delete_confirm_input").performTextInput("INVALID")
        composeRule.onNodeWithTag("profile_delete_confirm").assertIsNotEnabled()

        composeRule.onNodeWithTag("profile_delete_confirm_input").performTextClearance()
        composeRule.onNodeWithTag("profile_delete_confirm_input").performTextInput("HESABIMI SIL")
        composeRule.onNodeWithTag("profile_delete_confirm").performClick()
    }

    @Test
    fun tabNavigationResilience_shouldNotBreakCoreScreens() {
        completeOnboardingAndGuestLogin()

        composeRule.onNodeWithTag("tab_home").performClick()
        composeRule.onNodeWithTag("home_title").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_workout").performClick()
        composeRule.onNodeWithTag("workout_title").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_nutrition").performClick()
        composeRule.onNodeWithTag("nutrition_add_water").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_coach").performClick()
        composeRule.onNodeWithTag("coach_title").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.onNodeWithTag("profile_display_name").assertIsDisplayed()
    }

    private fun completeOnboardingAndGuestLogin() {
        if (hasNode("onboarding_next")) {
            composeRule.onNodeWithTag("onboarding_next").performClick()
            composeRule.onNodeWithTag("onboarding_name").performTextInput("Scenario User")
            composeRule.onNodeWithTag("onboarding_age").performTextInput("31")
            composeRule.onNodeWithTag("onboarding_height").performTextInput("180")
            composeRule.onNodeWithTag("onboarding_weight").performTextInput("82")
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
        composeRule.waitUntil(timeoutMillis = 20_000) {
            hasNode("tab_home") && hasNode("tab_workout") && hasNode("tab_nutrition") && hasNode("tab_coach")
        }
    }

    private fun hasNode(tag: String): Boolean =
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
