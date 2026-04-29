package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val email: String? = null,
    @ColumnInfo(name = "display_name")
    val displayName: String = "",
    val age: Int? = null,
    @ColumnInfo(name = "height_cm")
    val heightCm: Float? = null,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Float? = null,
    val gender: String? = null,
    @ColumnInfo(name = "activity_level")
    val activityLevel: String? = null,
    @ColumnInfo(name = "goal_type")
    val goalType: String? = null,
    @ColumnInfo(name = "target_calories")
    val targetCalories: Int? = null,
    @ColumnInfo(name = "protein_grams")
    val proteinGrams: Int? = null,
    @ColumnInfo(name = "carb_grams")
    val carbGrams: Int? = null,
    @ColumnInfo(name = "fat_grams")
    val fatGrams: Int? = null,
    val theme: String = "SYSTEM",
    val language: String = "TR",
    @ColumnInfo(name = "weight_unit")
    val weightUnit: String = "KG",
    @ColumnInfo(name = "height_unit")
    val heightUnit: String = "CM",
    @ColumnInfo(name = "is_guest")
    val isGuest: Boolean = false,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
)
