package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.WeeklyGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyGoalDao {
    @Query("SELECT * FROM weekly_goal_settings WHERE user_id = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<WeeklyGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeeklyGoalEntity)
}
