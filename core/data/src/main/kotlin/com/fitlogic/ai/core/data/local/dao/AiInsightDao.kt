package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.AiInsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiInsightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: AiInsightEntity)

    @Query("SELECT * FROM ai_insights WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    fun observeByUserId(
        userId: String,
        limit: Int,
    ): Flow<List<AiInsightEntity>>

    @Query("SELECT * FROM ai_insights WHERE id = :insightId LIMIT 1")
    fun observeById(insightId: String): Flow<AiInsightEntity?>

    @Query(
        """
        UPDATE ai_insights
        SET read_at = :readAt,
            updated_at = :updatedAt,
            sync_status = 'PENDING'
        WHERE id = :insightId
        """,
    )
    suspend fun markAsRead(
        insightId: String,
        readAt: Long,
        updatedAt: Long,
    )
}
