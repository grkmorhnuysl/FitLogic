package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "food_entries",
    indices = [Index(value = ["user_id", "eaten_at"]), Index(value = ["food_id"])],
)
data class FoodEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "food_id")
    val foodId: String,
    @ColumnInfo(name = "food_name")
    val foodName: String,
    @ColumnInfo(name = "meal_type")
    val mealType: String,
    val grams: Float,
    val kcal: Float,
    val protein: Float,
    val carb: Float,
    val fat: Float,
    @ColumnInfo(name = "eaten_at")
    val eatenAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
)
