package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.WaterEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntryEntity)

    @Query(
        """
        SELECT * FROM water_entries
        WHERE user_id = :userId
          AND consumed_at BETWEEN :dayStartEpochMs AND :dayEndEpochMs
        ORDER BY consumed_at DESC
        """,
    )
    fun observeByDay(
        userId: String,
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<List<WaterEntryEntity>>

    @Query(
        """
        SELECT IFNULL(SUM(amount_ml), 0)
        FROM water_entries
        WHERE user_id = :userId
          AND consumed_at BETWEEN :dayStartEpochMs AND :dayEndEpochMs
        """,
    )
    fun observeDailyTotalMl(
        userId: String,
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<Int>
}
