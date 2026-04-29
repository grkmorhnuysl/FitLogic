package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodEntryEntity)

    @Query(
        """
        SELECT * FROM food_entries
        WHERE user_id = :userId
          AND eaten_at BETWEEN :dayStartEpochMs AND :dayEndEpochMs
        ORDER BY eaten_at DESC
        """,
    )
    fun observeByDay(
        userId: String,
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<List<FoodEntryEntity>>

    @Query(
        """
        SELECT IFNULL(SUM(kcal), 0) AS calories,
               IFNULL(SUM(protein), 0) AS protein,
               IFNULL(SUM(carb), 0) AS carb,
               IFNULL(SUM(fat), 0) AS fat
        FROM food_entries
        WHERE user_id = :userId
          AND eaten_at BETWEEN :dayStartEpochMs AND :dayEndEpochMs
        """,
    )
    fun observeDailyTotals(
        userId: String,
        dayStartEpochMs: Long,
        dayEndEpochMs: Long,
    ): Flow<DailyFoodTotals>
}

data class DailyFoodTotals(
    val calories: Float,
    val protein: Float,
    val carb: Float,
    val fat: Float,
)
