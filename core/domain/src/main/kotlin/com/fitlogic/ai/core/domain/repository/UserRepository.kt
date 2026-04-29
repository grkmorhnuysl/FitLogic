package com.fitlogic.ai.core.domain.repository

import com.fitlogic.ai.core.domain.model.AppEntryDestination
import com.fitlogic.ai.core.domain.model.OnboardingDraft
import com.fitlogic.ai.core.domain.model.UserAuthState
import com.fitlogic.ai.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface UserRepository {
    fun observeAuthState(): Flow<UserAuthState>

    fun observeCurrentUserProfile(): Flow<UserProfile?>

    fun observeEntryDestination(): Flow<AppEntryDestination>

    suspend fun completeOnboarding(draft: OnboardingDraft): Result<Unit>

    suspend fun signIn(
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun register(
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun resetPassword(email: String): Result<Unit>

    suspend fun continueAsGuest(): Result<Unit>

    suspend fun updateProfile(profile: UserProfile): Result<Unit>

    suspend fun updateUserPreferences(profile: UserProfile): Result<Unit>

    suspend fun signOut(): Result<Unit>

    suspend fun deleteAccount(): Result<Unit>
}
