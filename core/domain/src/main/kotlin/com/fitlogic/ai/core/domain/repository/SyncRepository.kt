package com.fitlogic.ai.core.domain.repository

import com.fitlogic.ai.core.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun observeSyncStatus(): Flow<SyncStatus>

    suspend fun triggerSyncNow(): Result<Unit>

    suspend fun ensurePeriodicSync(): Result<Unit>

    suspend fun triggerInitialPullIfNeeded(): Result<Unit>
}
