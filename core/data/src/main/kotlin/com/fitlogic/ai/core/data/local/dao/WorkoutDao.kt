@file:Suppress("MaxLineLength")

package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

data class WeeklyVolumeRow(
    val weekLabel: String,
    val totalVolume: Float,
)

data class WeeklySummaryRow(
    val workoutsCompleted: Int,
    val totalVolume: Float,
    val avgDurationMinutes: Long,
)

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE user_id = :userId AND status = 'ACTIVE' ORDER BY started_at DESC LIMIT 1")
    fun observeActiveWorkout(userId: String): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE user_id = :userId AND status = 'ACTIVE' ORDER BY started_at DESC LIMIT 1")
    suspend fun getActiveWorkout(userId: String): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    suspend fun getById(workoutId: String): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    fun observeById(workoutId: String): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE user_id = :userId AND status = 'FINISHED' ORDER BY started_at DESC LIMIT :limit")
    fun observeHistory(
        userId: String,
        limit: Int,
    ): Flow<List<WorkoutEntity>>

    @Query(
        """
        UPDATE workouts
        SET status = 'FINISHED',
            finished_at = :finishedAt,
            updated_at = :updatedAt,
            sync_status = 'PENDING'
        WHERE id = :workoutId
        """,
    )
    suspend fun markFinished(
        workoutId: String,
        finishedAt: Long,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE workouts
        SET total_volume = :totalVolume,
            updated_at = :updatedAt,
            sync_status = 'PENDING'
        WHERE id = :workoutId
        """,
    )
    suspend fun updateTotalVolume(
        workoutId: String,
        totalVolume: Float,
        updatedAt: Long,
    )

    @Query(
        """
        SELECT
            strftime('%Y-W%W', started_at / 1000, 'unixepoch', 'localtime') AS weekLabel,
            COALESCE(SUM(total_volume), 0) AS totalVolume
        FROM workouts
        WHERE user_id = :userId
          AND status = 'FINISHED'
        GROUP BY weekLabel
        ORDER BY MAX(started_at) DESC
        LIMIT :weekCount
        """,
    )
    fun observeWeeklyVolume(
        userId: String,
        weekCount: Int,
    ): Flow<List<WeeklyVolumeRow>>

    @Query(
        """
        SELECT
            COUNT(*) AS workoutsCompleted,
            COALESCE(SUM(total_volume), 0) AS totalVolume,
            COALESCE(AVG((finished_at - started_at) / 60000), 0) AS avgDurationMinutes
        FROM workouts
        WHERE user_id = :userId
          AND status = 'FINISHED'
          AND started_at >= :fromEpochMs
        """,
    )
    fun observeWeeklySummary(
        userId: String,
        fromEpochMs: Long,
    ): Flow<WeeklySummaryRow>

    @Query(
        """
        SELECT started_at
        FROM workouts
        WHERE user_id = :userId
          AND status = 'FINISHED'
        ORDER BY started_at ASC
        """,
    )
    fun observeFinishedWorkoutTimestamps(userId: String): Flow<List<Long>>

    @Query(
        """
        SELECT COUNT(*)
        FROM workouts
        WHERE user_id = :userId
          AND status = 'FINISHED'
          AND started_at >= :fromEpochMs
        """,
    )
    fun observeFinishedWorkoutCountSince(
        userId: String,
        fromEpochMs: Long,
    ): Flow<Int>
}
