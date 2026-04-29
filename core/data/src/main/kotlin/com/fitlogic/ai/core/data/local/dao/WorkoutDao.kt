@file:Suppress("MaxLineLength")

package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

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
}
