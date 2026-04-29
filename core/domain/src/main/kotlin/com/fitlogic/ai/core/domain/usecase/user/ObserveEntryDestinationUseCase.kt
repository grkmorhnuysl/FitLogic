package com.fitlogic.ai.core.domain.usecase.user

import com.fitlogic.ai.core.domain.model.AppEntryDestination
import com.fitlogic.ai.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEntryDestinationUseCase
    @Inject
    constructor(
        private val userRepository: UserRepository,
    ) {
        operator fun invoke(): Flow<AppEntryDestination> = userRepository.observeEntryDestination()
    }
