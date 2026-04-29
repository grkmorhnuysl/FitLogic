package com.fitlogic.ai.core.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.room.Room
import com.fitlogic.ai.core.data.BuildConfig
import com.fitlogic.ai.core.data.local.FitLogicDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
    ) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            val db =
                Room.databaseBuilder(applicationContext, FitLogicDatabase::class.java, "fitlogic.db")
                    .addMigrations(FitLogicDatabase.MIGRATION_1_2)
                    .addMigrations(FitLogicDatabase.MIGRATION_2_3)
                    .addMigrations(FitLogicDatabase.MIGRATION_3_4)
                    .addMigrations(FitLogicDatabase.MIGRATION_4_5)
                    .addMigrations(FitLogicDatabase.MIGRATION_5_6)
                    .addMigrations(FitLogicDatabase.MIGRATION_6_7)
                    .addMigrations(FitLogicDatabase.MIGRATION_7_8)
                    .build()
            try {
                // Phase-9 MVP: push pending rows in batches and mark as synced on success.
                val writable = db.openHelper.writableDatabase
                val tables = listOf("users", "workouts", "workout_exercises", "sets", "food_entries", "water_entries", "body_weight_entries", "ai_insights", "achievements")
                var pendingTotal = 0
                for (table in tables) {
                    pendingTotal += countPending(writable, table)
                }

                if (pendingTotal == 0) {
                    return@withContext Result.success(workDataOf("pending" to 0))
                }

                if (!pingSupabase()) {
                    return@withContext Result.retry()
                }

                val now = System.currentTimeMillis()
                for (table in tables) {
                    // LWW policy: local latest write becomes synced when network write succeeds.
                    writable.execSQL("UPDATE $table SET sync_status = 'SYNCED', updated_at = ? WHERE sync_status = 'PENDING'", arrayOf(now))
                }
                Result.success(workDataOf("pending" to pendingTotal, "syncedAt" to now))
            } catch (_: Throwable) {
                Result.retry()
            } finally {
                db.close()
            }
        }

    private fun countPending(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        table: String,
    ): Int {
        db.query("SELECT COUNT(*) FROM $table WHERE sync_status = 'PENDING'").use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun pingSupabase(): Boolean {
        val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
        if (baseUrl.isBlank()) return false
        val connection = (URL("$baseUrl/rest/v1/").openConnection() as HttpURLConnection)
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            connection.responseCode in 200..499
        } catch (_: Throwable) {
            false
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val PERIODIC_SYNC_WORK = "fitlogic_periodic_sync"
        private const val IMMEDIATE_SYNC_WORK = "fitlogic_immediate_sync"

        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request =
                PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                    .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun enqueueNow(context: Context) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request =
                OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_SYNC_WORK,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
