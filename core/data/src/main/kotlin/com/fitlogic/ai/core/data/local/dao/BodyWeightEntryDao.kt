package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.BodyWeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BodyWeightEntryEntity)

    @Query(
        """
        SELECT *
        FROM body_weight_entries
        WHERE user_id = :userId
        ORDER BY measured_at DESC
        LIMIT :limit
        """,
    )
    fun observeByUserId(
        userId: String,
        limit: Int,
    ): Flow<List<BodyWeightEntryEntity>>
}
