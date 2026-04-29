package com.fitlogic.ai.core.domain.usecase.user

import com.fitlogic.ai.core.domain.model.OnboardingDraft
import com.fitlogic.ai.core.domain.repository.UserRepository
import javax.inject.Inject

class CompleteOnboardingUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        suspend operator fun invoke(draft: OnboardingDraft): Result<Unit> = userRepository.completeOnboarding(draft)
    }
