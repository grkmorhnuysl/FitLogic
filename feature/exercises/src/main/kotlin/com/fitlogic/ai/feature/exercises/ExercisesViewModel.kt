package com.fitlogic.ai.feature.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.usecase.workout.AddExerciseToWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.EnsureExerciseCatalogSeededUseCase
import com.fitlogic.ai.core.domain.usecase.workout.GetExerciseDetailUseCase
import com.fitlogic.ai.core.domain.usecase.workout.ObserveActiveWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.SearchExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
class ExercisesViewModel
    @Inject
    constructor(
        private val searchExercisesUseCase: SearchExercisesUseCase,
        private val getExerciseDetailUseCase: GetExerciseDetailUseCase,
        private val ensureExerciseCatalogSeededUseCase: EnsureExerciseCatalogSeededUseCase,
        private val addExerciseToWorkoutUseCase: AddExerciseToWorkoutUseCase,
        private val observeActiveWorkoutUseCase: ObserveActiveWorkoutUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ExercisesUiState())
        val uiState: StateFlow<ExercisesUiState> = _uiState.asStateFlow()

        init {
            collectActiveWorkout()
            viewModelScope.launch {
                ensureExerciseCatalogSeededUseCase()
                searchExercises()
            }
        }

        fun onQueryChange(query: String) {
            _uiState.update { it.copy(query = query) }
            viewModelScope.launch { searchExercises() }
        }

        fun toggleMuscleGroup(group: String) {
            _uiState.update { state ->
                val updated =
                    if (group in state.selectedMuscleGroups) {
                        state.selectedMuscleGroups - group
                    } else {
                        state.selectedMuscleGroups + group
                    }
                state.copy(selectedMuscleGroups = updated)
            }
            viewModelScope.launch { searchExercises() }
        }

        fun toggleEquipment(equipment: String) {
            _uiState.update { state ->
                val updated =
                    if (equipment in state.selectedEquipments) {
                        state.selectedEquipments - equipment
                    } else {
                        state.selectedEquipments + equipment
                    }
                state.copy(selectedEquipments = updated)
            }
            viewModelScope.launch { searchExercises() }
        }

        fun setDifficulty(difficulty: String?) {
            val newValue = if (_uiState.value.selectedDifficulty == difficulty) null else difficulty
            _uiState.update { it.copy(selectedDifficulty = newValue) }
            viewModelScope.launch { searchExercises() }
        }

        fun loadExerciseDetail(exerciseId: String) {
            viewModelScope.launch {
                _uiState.update {
                    it.copy(isLoading = true, selectedExercise = null, alternatives = emptyList(), selectedTab = 0)
                }
                val result = getExerciseDetailUseCase(exerciseId)
                result.fold(
                    onSuccess = { exercise ->
                        _uiState.update { it.copy(isLoading = false, selectedExercise = exercise) }
                        loadAlternatives(exercise.alternativeExerciseIds)
                    },
                    onFailure = { error ->
                        val msg = error.message ?: "Egzersiz yuklenemedi."
                        _uiState.update { it.copy(isLoading = false, message = msg) }
                    },
                )
            }
        }

        fun selectTab(index: Int) {
            _uiState.update { it.copy(selectedTab = index) }
        }

        fun addToWorkout(exerciseId: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isAddingToWorkout = true) }
                addExerciseToWorkoutUseCase(exerciseId).fold(
                    onSuccess = {
                        _uiState.update { it.copy(isAddingToWorkout = false, message = "Egzersiz antrenmana eklendi.") }
                    },
                    onFailure = { error ->
                        val msg = error.message ?: "Egzersiz eklenemedi."
                        _uiState.update { it.copy(isAddingToWorkout = false, message = msg) }
                    },
                )
            }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }

        private fun collectActiveWorkout() {
            viewModelScope.launch {
                observeActiveWorkoutUseCase()
                    .catch { _uiState.update { it.copy(hasActiveWorkout = false) } }
                    .collect { session ->
                        _uiState.update { it.copy(hasActiveWorkout = session != null) }
                    }
            }
        }

        private suspend fun searchExercises() {
            val state = _uiState.value
            val result =
                searchExercisesUseCase(
                    query = state.query,
                    muscleGroups = state.selectedMuscleGroups.toList(),
                    equipments = state.selectedEquipments.toList(),
                    difficulty = state.selectedDifficulty,
                    limit = 50,
                )
            _uiState.update {
                it.copy(
                    results = result.getOrDefault(emptyList()),
                    message = result.exceptionOrNull()?.message ?: it.message,
                )
            }
        }

        private fun loadAlternatives(ids: List<String>) {
            if (ids.isEmpty()) return
            viewModelScope.launch {
                val loaded =
                    ids.mapNotNull { id ->
                        getExerciseDetailUseCase(id).getOrNull()
                    }
                _uiState.update { it.copy(alternatives = loaded) }
            }
        }
    }
