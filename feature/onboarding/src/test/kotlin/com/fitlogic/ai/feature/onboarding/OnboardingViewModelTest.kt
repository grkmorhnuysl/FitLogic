package com.fitlogic.ai.feature.onboarding

import com.fitlogic.ai.core.domain.model.ActivityLevel
import com.fitlogic.ai.core.domain.model.AppEntryDestination
import com.fitlogic.ai.core.domain.model.Gender
import com.fitlogic.ai.core.domain.model.GoalType
import com.fitlogic.ai.core.domain.model.OnboardingDraft
import com.fitlogic.ai.core.domain.model.UserAuthState
import com.fitlogic.ai.core.domain.model.UserProfile
import com.fitlogic.ai.core.domain.repository.UserRepository
import com.fitlogic.ai.core.domain.usecase.user.CalculateMacroTargetsUseCase
import com.fitlogic.ai.core.domain.usecase.user.CompleteOnboardingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `should move through all steps and complete`() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            val vm =
                OnboardingViewModel(
                    calculateMacroTargetsUseCase = CalculateMacroTargetsUseCase(),
                    completeOnboardingUseCase = CompleteOnboardingUseCase(FakeOnboardingRepository()),
                )
            vm.nextStep()
            assertEquals(OnboardingStep.BASIC_INFO, vm.uiState.value.step)

            vm.onDisplayNameChange("Gorkem")
            vm.onAgeChange("30")
            vm.onHeightChange("178")
            vm.onWeightChange("80")
            vm.onGenderChange(Gender.MALE)
            vm.nextStep()
            assertEquals(OnboardingStep.GOAL, vm.uiState.value.step)

            vm.onActivityLevelChange(ActivityLevel.MODERATE)
            vm.onGoalTypeChange(GoalType.BUILD_MUSCLE)
            vm.nextStep()
            assertEquals(OnboardingStep.SUMMARY, vm.uiState.value.step)

            vm.complete()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(vm.uiState.value.isCompleted)
            Dispatchers.resetMain()
        }
}

private class FakeOnboardingRepository : UserRepository {
    override fun observeAuthState(): Flow<UserAuthState> = flowOf(UserAuthState.SignedOut)

    override fun observeCurrentUserProfile(): Flow<UserProfile?> = flowOf(null)

    override fun observeEntryDestination(): Flow<AppEntryDestination> = flowOf(AppEntryDestination.ONBOARDING)

    override suspend fun completeOnboarding(draft: OnboardingDraft): Result<Unit> = Result.success(Unit)

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun register(
        email: String,
        password: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun resetPassword(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)

    override suspend fun continueAsGuest(): Result<Unit> = Result.success(Unit)

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> = Result.success(Unit)

    override suspend fun updateUserPreferences(profile: UserProfile): Result<Unit> = Result.success(Unit)

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}
