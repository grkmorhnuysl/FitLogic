package com.fitlogic.ai.feature.exercises

import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem

val MuscleGroups = listOf("Chest", "Shoulders", "Back", "Arms", "Legs", "Core", "Conditioning")
val EquipmentTypes = listOf("Barbell", "Dumbbell", "Bodyweight", "Cable", "Machine")
val DifficultyLevels = listOf("Beginner", "Intermediate", "Advanced")

data class ExercisesUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val query: String = "",
    val selectedMuscleGroups: Set<String> = emptySet(),
    val selectedEquipments: Set<String> = emptySet(),
    val selectedDifficulty: String? = null,
    val results: List<ExerciseCatalogItem> = emptyList(),
    val selectedExercise: ExerciseCatalogItem? = null,
    val alternatives: List<ExerciseCatalogItem> = emptyList(),
    val selectedTab: Int = 0,
    val hasActiveWorkout: Boolean = false,
    val isAddingToWorkout: Boolean = false,
)
