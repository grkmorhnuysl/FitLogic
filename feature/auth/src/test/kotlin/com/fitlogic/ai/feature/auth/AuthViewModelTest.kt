package com.fitlogic.ai.feature.auth

import com.fitlogic.ai.core.domain.model.AppEntryDestination
import com.fitlogic.ai.core.domain.model.OnboardingDraft
import com.fitlogic.ai.core.domain.model.UserAuthState
import com.fitlogic.ai.core.domain.model.UserProfile
import com.fitlogic.ai.core.domain.repository.UserRepository
import com.fitlogic.ai.core.domain.usecase.user.ContinueAsGuestUseCase
import com.fitlogic.ai.core.domain.usecase.user.RegisterUseCase
import com.fitlogic.ai.core.domain.usecase.user.ResetPasswordUseCase
import com.fitlogic.ai.core.domain.usecase.user.SignInUseCase
import com.fitlogic.ai.core.domain.usecase.user.SignInWithGoogleUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `should fail when email is invalid`() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            val vm = createViewModel()
            vm.onEmailChange("invalid")
            vm.onPasswordChange("123456")

            vm.submit()

            assertEquals("Geçerli bir email adresi girin.", vm.uiState.value.message)
            Dispatchers.resetMain()
        }

    @Test
    fun `should sign in with valid form`() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            val vm = createViewModel()
            vm.onEmailChange("test@example.com")
            vm.onPasswordChange("123456")

            vm.submit()
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(vm.uiState.value.isAuthenticated)
            Dispatchers.resetMain()
        }

    @Test
    fun `should sign in with google token`() =
        runTest {
            Dispatchers.setMain(testDispatcher)
            val vm = createViewModel()

            vm.signInWithGoogle("id-token")
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(vm.uiState.value.isAuthenticated)
            Dispatchers.resetMain()
        }

    private fun createViewModel(): AuthViewModel {
        val repo = FakeUserRepository()
        return AuthViewModel(
            signInUseCase = SignInUseCase(repo),
            registerUseCase = RegisterUseCase(repo),
            resetPasswordUseCase = ResetPasswordUseCase(repo),
            continueAsGuestUseCase = ContinueAsGuestUseCase(repo),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(repo),
        )
    }
}

private class FakeUserRepository : UserRepository {
    private val auth = MutableStateFlow<UserAuthState>(UserAuthState.SignedOut)

    override fun observeAuthState(): Flow<UserAuthState> = auth

    override fun observeCurrentUserProfile(): Flow<UserProfile?> = flowOf(null)

    override fun observeEntryDestination(): Flow<AppEntryDestination> = flowOf(AppEntryDestination.AUTH)

    override suspend fun completeOnboarding(draft: OnboardingDraft): Result<Unit> = Result.success(Unit)

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<Unit> {
        auth.value = UserAuthState.Authenticated("id", email)
        return Result.success(Unit)
    }

    override suspend fun register(
        email: String,
        password: String,
    ): Result<Unit> = signIn(email, password)

    override suspend fun resetPassword(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        auth.value = UserAuthState.Authenticated("id", "google@example.com")
        return Result.success(Unit)
    }

    override suspend fun continueAsGuest(): Result<Unit> {
        auth.value = UserAuthState.Guest
        return Result.success(Unit)
    }

    override suspend fun updateProfile(profile: UserProfile): Result<Unit> = Result.success(Unit)

    override suspend fun updateUserPreferences(profile: UserProfile): Result<Unit> = Result.success(Unit)

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}
