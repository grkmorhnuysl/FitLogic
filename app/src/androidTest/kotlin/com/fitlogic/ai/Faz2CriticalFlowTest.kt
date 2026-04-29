package com.fitlogic.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class Faz2CriticalFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingToHomeToProfileEdit_shouldPersistDisplayName() {
        if (hasNode("onboarding_next")) {
            composeRule.onNodeWithTag("onboarding_next").performClick()

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
        composeRule.onNodeWithTag("home_title").assertIsDisplayed()

        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.onNodeWithTag("profile_display_name").performTextClearance()
        composeRule.onNodeWithTag("profile_display_name").performTextInput("Flow User")
        composeRule.onNodeWithTag("profile_save").performClick()

        composeRule.onNodeWithTag("tab_home").performClick()
        composeRule.onNodeWithTag("tab_profile").performClick()
        composeRule.onNodeWithTag("profile_display_name").assertTextContains("Flow User")
    }

    private fun hasNode(tag: String): Boolean =
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
}
