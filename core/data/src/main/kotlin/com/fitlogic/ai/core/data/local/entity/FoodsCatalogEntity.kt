package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "foods_catalog",
    indices = [Index(value = ["name"]), Index(value = ["barcode"], unique = true), Index(value = ["is_favorite"])],
)
data class FoodsCatalogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    @ColumnInfo(name = "brand_name")
    val brandName: String?,
    val barcode: String?,
    @ColumnInfo(name = "kcal_per_100g")
    val kcalPer100g: Float,
    @ColumnInfo(name = "protein_per_100g")
    val proteinPer100g: Float,
    @ColumnInfo(name = "carb_per_100g")
    val carbPer100g: Float,
    @ColumnInfo(name = "fat_per_100g")
    val fatPer100g: Float,
    val source: String = "LOCAL",
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
)
