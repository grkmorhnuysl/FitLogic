package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.ExercisesCatalogEntity

@Dao
interface ExercisesCatalogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExercisesCatalogEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM exercises_catalog")
    suspend fun count(): Int

    @Query(
        """
        SELECT * FROM exercises_catalog
        WHERE (:query = '' OR name LIKE '%' || :query || '%')
        AND ('__all__' IN (:muscleGroups) OR muscle_group IN (:muscleGroups))
        AND ('__all__' IN (:equipments) OR equipment IN (:equipments))
        AND (:difficulty = '' OR difficulty = :difficulty)
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun search(
        query: String,
        muscleGroups: List<String>,
        equipments: List<String>,
        difficulty: String,
        limit: Int,
    ): List<ExercisesCatalogEntity>

    @Query("SELECT * FROM exercises_catalog WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExercisesCatalogEntity?

    @Query(
        """
        SELECT * FROM exercises_catalog
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    suspend fun getTemplateExercises(limit: Int): List<ExercisesCatalogEntity>
}
