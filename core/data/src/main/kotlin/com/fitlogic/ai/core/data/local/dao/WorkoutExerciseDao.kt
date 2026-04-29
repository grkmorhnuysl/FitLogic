package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workoutExercise: WorkoutExerciseEntity)

    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId ORDER BY order_in_workout ASC")
    fun observeByWorkoutId(workoutId: String): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE workout_id = :workoutId ORDER BY order_in_workout ASC")
    suspend fun getByWorkoutId(workoutId: String): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM workout_exercises WHERE id = :workoutExerciseId LIMIT 1")
    suspend fun getById(workoutExerciseId: String): WorkoutExerciseEntity?

    @Query("SELECT COALESCE(MAX(order_in_workout), -1) FROM workout_exercises WHERE workout_id = :workoutId")
    suspend fun getMaxOrder(workoutId: String): Int
}
