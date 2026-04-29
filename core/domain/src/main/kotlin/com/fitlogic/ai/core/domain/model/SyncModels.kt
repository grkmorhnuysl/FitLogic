package com.fitlogic.ai.core.domain.model

data class SyncStatus(
    val pendingCount: Int = 0,
    val lastSyncAtEpochMs: Long? = null,
    val lastPullAtEpochMs: Long? = null,
    val lastError: String? = null,
    val isSyncInProgress: Boolean = false,
)
