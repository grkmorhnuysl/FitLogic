package com.fitlogic.ai.core.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fitlogic.ai.core.data.local.FitLogicDatabase
import com.fitlogic.ai.core.data.sync.SyncWorker
import com.fitlogic.ai.core.domain.model.SyncStatus
import com.fitlogic.ai.core.domain.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl
    @Inject
    constructor(
        private val database: FitLogicDatabase,
        @ApplicationContext private val context: Context,
    ) : SyncRepository {
        override fun observeSyncStatus(): Flow<SyncStatus> {
            val pendingCounts =
                combine(
                    database.invalidationTracker.createFlow("users"),
                    database.invalidationTracker.createFlow("workouts"),
                    database.invalidationTracker.createFlow("workout_exercises"),
                    database.invalidationTracker.createFlow("sets"),
                    database.invalidationTracker.createFlow("food_entries"),
                    database.invalidationTracker.createFlow("water_entries"),
                    database.invalidationTracker.createFlow("body_weight_entries"),
                    database.invalidationTracker.createFlow("ai_insights"),
                    database.invalidationTracker.createFlow("achievements"),
                ) { countPending() }
            val syncMeta =
                context.syncMetaStore.data.map { pref ->
                    Triple(
                        pref[LAST_SYNC_AT],
                        pref[LAST_PULL_AT],
                        pref[LAST_ERROR],
                    )
                }
            return combine(pendingCounts, syncMeta) { pending, meta ->
                SyncStatus(
                    pendingCount = pending,
                    lastSyncAtEpochMs = meta.first,
                    lastPullAtEpochMs = meta.second,
                    lastError = meta.third,
                    isSyncInProgress = false,
                )
            }
        }

        override suspend fun triggerSyncNow(): Result<Unit> =
            runCatching {
                SyncWorker.enqueueNow(context)
                context.syncMetaStore.edit { it.remove(LAST_ERROR) }
            }

        override suspend fun ensurePeriodicSync(): Result<Unit> =
            runCatching {
                SyncWorker.enqueuePeriodic(context)
            }

        override suspend fun triggerInitialPullIfNeeded(): Result<Unit> =
            runCatching {
                // lightweight first-login pull marker for phase-9 MVP
                val shouldPull = context.syncMetaStore.data.map { it[LAST_PULL_AT] == null }.first()
                if (shouldPull) {
                    context.syncMetaStore.edit { store -> store[LAST_PULL_AT] = System.currentTimeMillis() }
                }
            }

        private fun countPending(): Int {
            val db = database.openHelper.readableDatabase
            val tables = listOf("users", "workouts", "workout_exercises", "sets", "food_entries", "water_entries", "body_weight_entries", "ai_insights", "achievements")
            var count = 0
            for (table in tables) {
                db.query("SELECT COUNT(*) FROM $table WHERE sync_status = 'PENDING'").use { cursor ->
                    if (cursor.moveToFirst()) count += cursor.getInt(0)
                }
            }
            return count
        }

        companion object {
            private val LAST_SYNC_AT = longPreferencesKey("sync_last_sync_at")
            private val LAST_PULL_AT = longPreferencesKey("sync_last_pull_at")
            private val LAST_ERROR = stringPreferencesKey("sync_last_error")
        }
    }

private val Context.syncMetaStore by androidx.datastore.preferences.preferencesDataStore(name = "sync_meta.preferences_pb")
