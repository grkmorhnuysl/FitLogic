package com.fitlogic.ai.core.domain.usecase.user

import com.fitlogic.ai.core.domain.model.UserAuthState
import com.fitlogic.ai.core.domain.model.UserProfile
import com.fitlogic.ai.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        operator fun invoke(): Flow<UserAuthState> = userRepository.observeAuthState()
    }

class ObserveCurrentProfileUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        operator fun invoke(): Flow<UserProfile?> = userRepository.observeCurrentUserProfile()
    }

class SignInUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(
            email: String,
            password: String,
        ): Result<Unit> = userRepository.signIn(email, password)
    }

class RegisterUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(
            email: String,
            password: String,
        ): Result<Unit> = userRepository.register(email, password)
    }

class SignInWithGoogleUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(idToken: String): Result<Unit> = userRepository.signInWithGoogle(idToken)
    }

class ResetPasswordUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(email: String): Result<Unit> = userRepository.resetPassword(email)
    }

class ContinueAsGuestUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = userRepository.continueAsGuest()
    }

class UpdateProfileUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(profile: UserProfile): Result<Unit> = userRepository.updateProfile(profile)
    }

class UpdateUserPreferencesUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(profile: UserProfile): Result<Unit> = userRepository.updateUserPreferences(profile)
    }

class SignOutUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = userRepository.signOut()
    }

class DeleteAccountUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = userRepository.deleteAccount()
    }
