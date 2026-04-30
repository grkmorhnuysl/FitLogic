package com.fitlogic.ai.core.domain.usecase.ai

import com.fitlogic.ai.core.domain.repository.AiInsightRepository
import javax.inject.Inject

class SendCoachMessageUseCase
    @Inject
    constructor(
        private val aiInsightRepository: AiInsightRepository,
    ) {
        suspend operator fun invoke(message: String): Result<String> = aiInsightRepository.sendCoachMessage(message)
    }
