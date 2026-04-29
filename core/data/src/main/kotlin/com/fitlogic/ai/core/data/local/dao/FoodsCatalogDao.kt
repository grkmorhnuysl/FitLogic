package com.fitlogic.ai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitlogic.ai.core.data.local.entity.FoodsCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodsCatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodsCatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: FoodsCatalogEntity)

    @Query("SELECT COUNT(*) FROM foods_catalog")
    suspend fun count(): Int

    @Query("SELECT * FROM foods_catalog WHERE id = :foodId LIMIT 1")
    suspend fun getById(foodId: String): FoodsCatalogEntity?

    @Query(
        """
        SELECT * FROM foods_catalog
        WHERE name LIKE '%' || :query || '%'
           OR IFNULL(brand_name, '') LIKE '%' || :query || '%'
        ORDER BY is_favorite DESC, IFNULL(last_used_at, 0) DESC, name ASC
        LIMIT :limit
        """,
    )
    suspend fun search(
        query: String,
        limit: Int,
    ): List<FoodsCatalogEntity>

    @Query("SELECT * FROM foods_catalog WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): FoodsCatalogEntity?

    @Query("SELECT * FROM foods_catalog WHERE is_favorite = 1 ORDER BY name ASC")
    fun observeFavorites(): Flow<List<FoodsCatalogEntity>>

    @Query(
        """
        UPDATE foods_catalog
        SET is_favorite = :favorite, updated_at = :updatedAt, sync_status = 'PENDING'
        WHERE id = :foodId
        """,
    )
    suspend fun setFavorite(
        foodId: String,
        favorite: Boolean,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE foods_catalog
        SET last_used_at = :lastUsedAt, updated_at = :updatedAt
        WHERE id = :foodId
        """,
    )
    suspend fun touchLastUsed(
        foodId: String,
        lastUsedAt: Long,
        updatedAt: Long,
    )
}
