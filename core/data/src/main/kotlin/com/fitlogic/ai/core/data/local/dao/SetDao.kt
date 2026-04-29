package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.SetEntity
import kotlinx.coroutines.flow.Flow

data class ExerciseProgressRow(
    val dateEpochMs: Long,
    val totalVolume: Float,
    val bestWeightKg: Float,
)

data class MuscleGroupDistributionRow(
    val muscleGroup: String,
    val totalVolume: Float,
)

data class PrHistoryRow(
    val performedAt: Long,
    val exerciseName: String,
    val weightKg: Float,
    val reps: Int,
    val volume: Float,
)

@Dao
interface SetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: SetEntity)

    @Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY performed_at ASC")
    fun observeByWorkoutId(workoutId: String): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE workout_id = :workoutId ORDER BY performed_at ASC")
    suspend fun getByWorkoutId(workoutId: String): List<SetEntity>

    @Query("SELECT * FROM sets WHERE workout_exercise_id = :workoutExerciseId ORDER BY performed_at ASC")
    suspend fun getByWorkoutExerciseId(workoutExerciseId: String): List<SetEntity>

    @Query(
        """
        SELECT s.*
        FROM sets s
        INNER JOIN workouts w ON w.id = s.workout_id
        WHERE w.user_id = :userId
          AND s.exercise_catalog_id = :exerciseCatalogId
          AND w.status = 'FINISHED'
        ORDER BY s.volume DESC, s.performed_at DESC
        LIMIT 1
        """,
    )
    suspend fun getBestFinishedSetForExercise(
        userId: String,
        exerciseCatalogId: String,
    ): SetEntity?

    @Query(
        """
        SELECT s.*
        FROM sets s
        INNER JOIN workouts w ON w.id = s.workout_id
        WHERE w.user_id = :userId
          AND s.exercise_catalog_id = :exerciseCatalogId
          AND w.status = 'FINISHED'
        ORDER BY s.performed_at DESC
        LIMIT 1
        """,
    )
    suspend fun getLastFinishedSetForExercise(
        userId: String,
        exerciseCatalogId: String,
    ): SetEntity?

    @Query(
        """
        SELECT
            w.started_at AS dateEpochMs,
            COALESCE(SUM(s.volume), 0) AS totalVolume,
            COALESCE(MAX(s.weight_kg), 0) AS bestWeightKg
        FROM sets s
        INNER JOIN workouts w ON w.id = s.workout_id
        WHERE w.user_id = :userId
          AND w.status = 'FINISHED'
          AND s.exercise_catalog_id = :exerciseCatalogId
        GROUP BY w.id, w.started_at
        ORDER BY w.started_at ASC
        """,
    )
    fun observeExerciseProgress(
        userId: String,
        exerciseCatalogId: String,
    ): Flow<List<ExerciseProgressRow>>

    @Query(
        """
        SELECT
            ec.muscle_group AS muscleGroup,
            COALESCE(SUM(s.volume), 0) AS totalVolume
        FROM sets s
        INNER JOIN workouts w ON w.id = s.workout_id
        INNER JOIN exercises_catalog ec ON ec.id = s.exercise_catalog_id
        WHERE w.user_id = :userId
          AND w.status = 'FINISHED'
        GROUP BY ec.muscle_group
        ORDER BY totalVolume DESC
        """,
    )
    fun observeMuscleDistribution(userId: String): Flow<List<MuscleGroupDistributionRow>>

    @Query(
        """
        SELECT
            s.performed_at AS performedAt,
            we.exercise_name AS exerciseName,
            s.weight_kg AS weightKg,
            s.reps AS reps,
            s.volume AS volume
        FROM sets s
        INNER JOIN workouts w ON w.id = s.workout_id
        INNER JOIN workout_exercises we ON we.id = s.workout_exercise_id
        WHERE w.user_id = :userId
          AND w.status = 'FINISHED'
          AND s.is_pr = 1
        ORDER BY s.performed_at DESC
        LIMIT :limit
        """,
    )
    fun observePrHistory(
        userId: String,
        limit: Int,
    ): Flow<List<PrHistoryRow>>

    @Query(
        """
        SELECT COUNT(*)
        FROM sets s
        INNER JOIN workouts w ON w.id = s.workout_id
        WHERE w.user_id = :userId
          AND w.status = 'FINISHED'
          AND s.is_pr = 1
          AND s.performed_at >= :fromEpochMs
        """,
    )
    fun observeWeeklyPrCount(
        userId: String,
        fromEpochMs: Long,
    ): Flow<Int>
}
