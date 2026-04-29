package com.fitlogic.ai.core.data.repository

import com.fitlogic.ai.core.data.local.dao.UserDao
import com.fitlogic.ai.core.data.local.entity.UserEntity
import com.fitlogic.ai.core.data.local.mapper.toDomain
import com.fitlogic.ai.core.data.local.mapper.toEntity
import com.fitlogic.ai.core.data.local.session.SessionPreferences
import com.fitlogic.ai.core.data.remote.AuthDataSource
import com.fitlogic.ai.core.data.remote.AuthSession
import com.fitlogic.ai.core.data.remote.SessionTokens
import com.fitlogic.ai.core.domain.model.AppEntryDestination
import com.fitlogic.ai.core.domain.model.Gender
import com.fitlogic.ai.core.domain.model.OnboardingDraft
import com.fitlogic.ai.core.domain.model.UserAuthState
import com.fitlogic.ai.core.domain.model.UserProfile
import com.fitlogic.ai.core.domain.repository.UserRepository
import com.fitlogic.ai.core.domain.usecase.user.CalculateMacroTargetsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class UserRepositoryImpl
    @Inject
    constructor(
        private val userDao: UserDao,
        private val authDataSource: AuthDataSource,
        private val sessionPreferences: SessionPreferences,
        private val calculateMacroTargetsUseCase: CalculateMacroTargetsUseCase,
    ) : UserRepository {
        override fun observeAuthState(): Flow<UserAuthState> =
            sessionPreferences.sessionSnapshot.transformLatest { session ->
                val authState = session.authState
                if (
                    authState is UserAuthState.Authenticated &&
                    isSessionExpired(session.expiresAtEpochSeconds) &&
                    !session.refreshToken.isNullOrBlank()
                ) {
                    val refreshResult = authDataSource.refreshSession(session.refreshToken)
                    if (refreshResult.isSuccess) {
                        persistTokens(refreshResult.getOrThrow())
                    } else {
                        sessionPreferences.setSignedOut()
                        emit(UserAuthState.SignedOut)
                        return@transformLatest
                    }
                }
                emit(authState)
            }

        override fun observeCurrentUserProfile(): Flow<UserProfile?> =
            sessionPreferences.sessionSnapshot.flatMapLatest { session ->
                val userId = session.currentUserId ?: return@flatMapLatest flowOf(null)
                userDao.observeById(userId).mapToDomain(session.onboardingCompleted)
            }

        override fun observeEntryDestination(): Flow<AppEntryDestination> =
            combine(observeAuthState(), sessionPreferences.sessionSnapshot) { authState, session ->
                when {
                    !session.onboardingCompleted -> AppEntryDestination.ONBOARDING
                    authState is UserAuthState.Authenticated -> AppEntryDestination.HOME
                    authState is UserAuthState.Guest -> AppEntryDestination.HOME
                    else -> AppEntryDestination.AUTH
                }
            }

        override suspend fun completeOnboarding(draft: OnboardingDraft): Result<Unit> =
            runCatching {
                val currentUserId = sessionPreferences.sessionSnapshot.first().currentUserId
                val userId = currentUserId ?: "local-${UUID.randomUUID()}"
                val macroTargets =
                    draft.toMacroCalculationInput()?.let { input ->
                        calculateMacroTargetsUseCase(
                            gender = input.gender,
                            age = input.age,
                            heightCm = input.heightCm,
                            weightKg = input.weightKg,
                            activityLevel = draft.activityLevel,
                            goalType = draft.goalType,
                        )
                    }
                val existing = userDao.getById(userId)
                val userEntity =
                    existing?.copy(
                        displayName = draft.displayName,
                        age = draft.age,
                        heightCm = draft.heightCm,
                        weightKg = draft.weightKg,
                        gender = draft.gender?.name,
                        activityLevel = draft.activityLevel.name,
                        goalType = draft.goalType.name,
                        targetCalories = macroTargets?.calories,
                        proteinGrams = macroTargets?.proteinGrams,
                        carbGrams = macroTargets?.carbGrams,
                        fatGrams = macroTargets?.fatGrams,
                        updatedAt = System.currentTimeMillis(),
                    ) ?: UserEntity(
                        id = userId,
                        displayName = draft.displayName,
                        age = draft.age,
                        heightCm = draft.heightCm,
                        weightKg = draft.weightKg,
                        gender = draft.gender?.name,
                        activityLevel = draft.activityLevel.name,
                        goalType = draft.goalType.name,
                        targetCalories = macroTargets?.calories,
                        proteinGrams = macroTargets?.proteinGrams,
                        carbGrams = macroTargets?.carbGrams,
                        fatGrams = macroTargets?.fatGrams,
                    )
                userDao.upsert(userEntity)
                sessionPreferences.setCurrentUserId(userId)
                sessionPreferences.markOnboardingCompleted(completed = true)
            }

        override suspend fun signIn(
            email: String,
            password: String,
        ): Result<Unit> =
            authDataSource.signIn(email, password).mapCatching { session ->
                persistAuthenticatedSession(session)
            }

        override suspend fun register(
            email: String,
            password: String,
        ): Result<Unit> =
            authDataSource.register(email, password).mapCatching { session ->
                persistAuthenticatedSession(session)
            }

        override suspend fun signInWithGoogle(idToken: String): Result<Unit> =
            authDataSource.signInWithGoogleIdToken(idToken).mapCatching { session ->
                persistAuthenticatedSession(session)
            }

        override suspend fun resetPassword(email: String): Result<Unit> = authDataSource.resetPassword(email)

        override suspend fun continueAsGuest(): Result<Unit> =
            runCatching {
                val userId = "guest-local"
                val existing = userDao.getById(userId)
                val guestEntity =
                    existing?.copy(isGuest = true, updatedAt = System.currentTimeMillis()) ?: UserEntity(
                        id = userId,
                        email = null,
                        displayName = "Misafir",
                        isGuest = true,
                    )
                userDao.upsert(guestEntity)
                sessionPreferences.setCurrentUserId(userId)
                sessionPreferences.setGuest()
                sessionPreferences.markOnboardingCompleted(true)
            }

        override suspend fun updateProfile(profile: UserProfile): Result<Unit> =
            runCatching {
                userDao.upsert(profile.toEntity())
                sessionPreferences.setCurrentUserId(profile.id)
            }

        override suspend fun updateUserPreferences(profile: UserProfile): Result<Unit> = updateProfile(profile)

        override suspend fun signOut(): Result<Unit> {
            val accessToken = sessionPreferences.sessionSnapshot.first().accessToken
            return authDataSource.signOut(accessToken).mapCatching {
                sessionPreferences.setSignedOut()
            }
        }

        override suspend fun deleteAccount(): Result<Unit> {
            val snapshot = sessionPreferences.sessionSnapshot.first()
            return authDataSource.deleteCurrentAccount(snapshot.accessToken).mapCatching {
                val userId = snapshot.currentUserId
                if (userId != null) {
                    val existing = userDao.getById(userId)
                    if (existing != null) {
                        userDao.upsert(existing.copy(isDeleted = true, updatedAt = System.currentTimeMillis()))
                    }
                }
                sessionPreferences.clearAll()
                userDao.clearAll()
            }
        }

        private suspend fun persistAuthenticatedSession(session: AuthSession) {
            val existing = userDao.getById(session.user.id)
            val mapped =
                existing?.copy(
                    email = session.user.email,
                    isGuest = false,
                    updatedAt = System.currentTimeMillis(),
                ) ?: UserEntity(
                    id = session.user.id,
                    email = session.user.email,
                    displayName = session.user.email.substringBefore("@"),
                    isGuest = false,
                )
            userDao.upsert(mapped)
            sessionPreferences.setAuthenticated(
                userId = session.user.id,
                email = session.user.email,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                expiresAtEpochSeconds = session.expiresAtEpochSeconds,
                provider = session.provider,
            )
        }

        private suspend fun persistTokens(tokens: SessionTokens) {
            sessionPreferences.updateTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                expiresAtEpochSeconds = tokens.expiresAtEpochSeconds,
            )
        }

        private fun isSessionExpired(expiresAtEpochSeconds: Long?): Boolean {
            if (expiresAtEpochSeconds == null) return false
            return (System.currentTimeMillis() / 1000L) >= expiresAtEpochSeconds
        }

        private fun Flow<UserEntity?>.mapToDomain(onboardingCompleted: Boolean): Flow<UserProfile?> =
            map { entity -> entity?.toDomain(onboardingCompleted = onboardingCompleted) }

        private fun OnboardingDraft.toMacroCalculationInput(): MacroCalculationInput? {
            val hasRequiredFields = age != null && heightCm != null && weightKg != null && gender != null
            return if (hasRequiredFields) {
                MacroCalculationInput(
                    age = age!!,
                    heightCm = heightCm!!,
                    weightKg = weightKg!!,
                    gender = gender!!,
                )
            } else {
                null
            }
        }

        private data class MacroCalculationInput(
            val age: Int,
            val heightCm: Float,
            val weightKg: Float,
            val gender: Gender,
        )
    }
