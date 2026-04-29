package com.fitlogic.ai.feature.profile

import com.fitlogic.ai.core.domain.model.AppEntryDestination
import com.fitlogic.ai.core.domain.model.Achievement
import com.fitlogic.ai.core.domain.model.NotificationSettings
import com.fitlogic.ai.core.domain.model.NotificationType
import com.fitlogic.ai.core.domain.model.OnboardingDraft
import com.fitlogic.ai.core.domain.model.StreakInfo
import com.fitlogic.ai.core.domain.model.SyncStatus
import com.fitlogic.ai.core.domain.model.UserAuthState
import com.fitlogic.ai.core.domain.model.UserProfile
import com.fitlogic.ai.core.domain.model.WeeklyGoal
import com.fitlogic.ai.core.domain.repository.GamificationRepository
import com.fitlogic.ai.core.domain.repository.SyncRepository
import com.fitlogic.ai.core.domain.repository.UserRepository
import com.fitlogic.ai.core.domain.usecase.gamification.ObserveNotificationSettingsUseCase
import com.fitlogic.ai.core.domain.usecase.gamification.SetNotificationEnabledUseCase
import com.fitlogic.ai.core.domain.usecase.sync.ObserveSyncStatusUseCase
import com.fitlogic.ai.core.domain.usecase.sync.TriggerSyncNowUseCase
import com.fitlogic.ai.core.domain.usecase.user.DeleteAccountUseCase
import com.fitlogic.ai.core.domain.usecase.user.ObserveCurrentProfileUseCase
import com.fitlogic.ai.core.domain.usecase.user.SignOutUseCase
import com.fitlogic.ai.core.domain.usecase.user.UpdateProfileUseCase
import com.fitlogic.ai.core.domain.usecase.user.UpdateUserPreferencesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun confirmDelete_shouldBlockWhenPhraseIsInvalid() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            val repo = FakeProfileRepository()
            val viewModel = createViewModel(repo)

            viewModel.requestDeleteAccount()
            viewModel.onDeleteConfirmInputChange("INVALID")
            viewModel.confirmDeleteAccount()
            testDispatcher.scheduler.advanceUntilIdle()

            assertFalse(repo.deleteCalled)
            assertEquals("Silme onayi icin HESABIMI SIL yazin.", viewModel.uiState.value.message)
            Dispatchers.resetMain()
        }

    @Test
    fun confirmDelete_shouldProceedWhenPhraseMatches() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            val repo = FakeProfileRepository()
            val viewModel = createViewModel(repo)

            viewModel.requestDeleteAccount()
            viewModel.onDeleteConfirmInputChange(ProfileViewModel.DELETE_CONFIRM_PHRASE)
            viewModel.confirmDeleteAccount()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(repo.deleteCalled)
            assertFalse(viewModel.uiState.value.isDeleteDialogVisible)
            Dispatchers.resetMain()
        }

    private fun createViewModel(repo: FakeProfileRepository): ProfileViewModel =
        ProfileViewModel(
            observeCurrentProfileUseCase = ObserveCurrentProfileUseCase(repo),
            updateProfileUseCase = UpdateProfileUseCase(repo),
            updateUserPreferencesUseCase = UpdateUserPreferencesUseCase(repo),
            signOutUseCase = SignOutUseCase(repo),
            deleteAccountUseCase = DeleteAccountUseCase(repo),
            observeNotificationSettingsUseCase = ObserveNotificationSettingsUseCase(repo),
            setNotificationEnabledUseCase = SetNotificationEnabledUseCase(repo),
            observeSyncStatusUseCase = ObserveSyncStatusUseCase(repo),
            triggerSyncNowUseCase = TriggerSyncNowUseCase(repo),
        )
}

private class FakeProfileRepository : UserRepository, GamificationRepository, SyncRepository {
    val profile = MutableStateFlow<UserProfile?>(null)
    var deleteCalled: Boolean = false

    override fun observeAuthState(): Flow<UserAuthState> = flowOf(UserAuthState.Authenticated("id", "mail@test.com"))

    override fun observeCurrentUserProfile(): Flow<UserProfile?> = profile

    override fun observeEntryDestination(): Flow<AppEntryDestination> = flowOf(AppEntryDestination.HOME)

    override suspend fun completeOnboarding(draft: OnboardingDraft): Result<Unit> = Result.success(Unit)

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun register(
        email: String,
        password: String,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)

    override suspend fun resetPassword(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun continueAsGuest(): Result<Unit> = Result.success(Unit)

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> = Result.success(Unit)

    override suspend fun updateUserPreferences(profile: UserProfile): Result<Unit> = Result.success(Unit)

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAccount(): Result<Unit> {
        deleteCalled = true
        return Result.success(Unit)
    }

    override fun observeStreak(): Flow<StreakInfo> = flowOf(StreakInfo(0, 0, null))

    override fun observeAchievements(): Flow<List<Achievement>> = flowOf(emptyList())

    override fun observeWeeklyGoal(): Flow<WeeklyGoal> = flowOf(WeeklyGoal(targetWorkouts = 3, completedWorkouts = 0))

    override fun observeNotificationSettings(): Flow<NotificationSettings> = flowOf(NotificationSettings())

    override suspend fun setWeeklyGoal(targetWorkouts: Int): Result<Unit> = Result.success(Unit)

    override suspend fun checkAchievements(): Result<List<Achievement>> = Result.success(emptyList())

    override suspend fun setNotificationEnabled(
        type: NotificationType,
        enabled: Boolean,
    ): Result<Unit> = Result.success(Unit)

    override fun observeSyncStatus(): Flow<SyncStatus> = flowOf(SyncStatus())

    override suspend fun triggerSyncNow(): Result<Unit> = Result.success(Unit)

    override suspend fun ensurePeriodicSync(): Result<Unit> = Result.success(Unit)

    override suspend fun triggerInitialPullIfNeeded(): Result<Unit> = Result.success(Unit)
}
