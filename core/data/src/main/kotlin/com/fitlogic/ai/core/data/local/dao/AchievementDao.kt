package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE user_id = :userId ORDER BY unlocked_at DESC, created_at ASC")
    fun observeByUserId(userId: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE user_id = :userId")
    suspend fun getByUserId(userId: String): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AchievementEntity>)
}
