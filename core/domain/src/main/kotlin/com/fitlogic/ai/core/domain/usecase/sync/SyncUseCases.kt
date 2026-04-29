package com.fitlogic.ai.core.domain.usecase.sync

import com.fitlogic.ai.core.domain.model.SyncStatus
import com.fitlogic.ai.core.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSyncStatusUseCase
    @Inject
    constructor(
        private val syncRepository: SyncRepository,
    ) {
        operator fun invoke(): Flow<SyncStatus> = syncRepository.observeSyncStatus()
    }

class TriggerSyncNowUseCase
    @Inject
    constructor(
        private val syncRepository: SyncRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = syncRepository.triggerSyncNow()
    }

class EnsurePeriodicSyncUseCase
    @Inject
    constructor(
        private val syncRepository: SyncRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = syncRepository.ensurePeriodicSync()
    }

class TriggerInitialPullIfNeededUseCase
    @Inject
    constructor(
        private val syncRepository: SyncRepository,
    ) {
        suspend operator fun invoke(): Result<Unit> = syncRepository.triggerInitialPullIfNeeded()
    }
