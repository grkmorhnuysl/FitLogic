package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.SetEntity
import kotlinx.coroutines.flow.Flow

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
}
