package com.fitlogic.ai.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises_catalog")
data class ExercisesCatalogEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "muscle_group")
    val muscleGroup: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
    @ColumnInfo(name = "instruction_steps_json")
    val instructionStepsJson: String,
    @ColumnInfo(name = "common_mistakes_json")
    val commonMistakesJson: String,
    @ColumnInfo(name = "alternative_exercise_ids_json")
    val alternativeExerciseIdsJson: String,
    @ColumnInfo(name = "gif_asset_path")
    val gifAssetPath: String,
)
