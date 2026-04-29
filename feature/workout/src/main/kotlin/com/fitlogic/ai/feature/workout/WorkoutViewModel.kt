package com.fitlogic.ai.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.usecase.workout.AddExerciseToWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.CopyLastSetUseCase
import com.fitlogic.ai.core.domain.usecase.workout.EnsureExerciseCatalogSeededUseCase
import com.fitlogic.ai.core.domain.usecase.workout.FinishWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.GetWorkoutHistoryUseCase
import com.fitlogic.ai.core.domain.usecase.workout.ObserveActiveWorkoutUseCase
import com.fitlogic.ai.core.domain.usecase.workout.ObserveWorkoutDetailUseCase
import com.fitlogic.ai.core.domain.usecase.workout.SaveSetUseCase
import com.fitlogic.ai.core.domain.usecase.workout.SearchExercisesUseCase
import com.fitlogic.ai.core.domain.usecase.workout.StartWorkoutFromHistoryUseCase
import com.fitlogic.ai.core.domain.usecase.workout.StartWorkoutFromTemplateUseCase
import com.fitlogic.ai.core.domain.usecase.workout.StartWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel
    @Inject
    constructor(
        private val observeActiveWorkoutUseCase: ObserveActiveWorkoutUseCase,
        private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
        private val observeWorkoutDetailUseCase: ObserveWorkoutDetailUseCase,
        private val ensureExerciseCatalogSeededUseCase: EnsureExerciseCatalogSeededUseCase,
        private val searchExercisesUseCase: SearchExercisesUseCase,
        private val startWorkoutUseCase: StartWorkoutUseCase,
        private val startWorkoutFromTemplateUseCase: StartWorkoutFromTemplateUseCase,
        private val startWorkoutFromHistoryUseCase: StartWorkoutFromHistoryUseCase,
        private val addExerciseToWorkoutUseCase: AddExerciseToWorkoutUseCase,
        private val saveSetUseCase: SaveSetUseCase,
        private val copyLastSetUseCase: CopyLastSetUseCase,
        private val finishWorkoutUseCase: FinishWorkoutUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(WorkoutUiState())
        val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

        private var detailJob: Job? = null

        init {
            collectActiveWorkout()
            collectHistory()
            seedAndLoadExercises()
        }

        fun startEmptyWorkout() {
            launchWithLoading {
                startWorkoutUseCase().unwrapOrMessage("Antrenman baslatilamadi.")
            }
        }

        fun startFromTemplate(templateName: String = "Genel") {
            launchWithLoading {
                startWorkoutFromTemplateUseCase(templateName).unwrapOrMessage("Sablondan antrenman baslatilamadi.")
            }
        }

        fun startFromHistory(workoutId: String) {
            launchWithLoading {
                startWorkoutFromHistoryUseCase(workoutId).unwrapOrMessage("Gecmisten antrenman tekrarlanamadi.")
            }
        }

        fun onExerciseQueryChange(query: String) {
            _uiState.update { it.copy(exerciseQuery = query) }
            viewModelScope.launch {
                val result = searchExercisesUseCase(query, limit = 20)
                _uiState.update {
                    it.copy(
                        exerciseResults = result.getOrDefault(emptyList()),
                        message = result.exceptionOrNull()?.message ?: it.message,
                    )
                }
            }
        }

        fun addExercise(exerciseCatalogId: String) {
            launchWithLoading {
                addExerciseToWorkoutUseCase(exerciseCatalogId).unwrapOrMessage("Egzersiz eklenemedi.")
            }
        }

        fun saveSet(
            workoutExerciseId: String,
            weightRaw: String,
            repsRaw: String,
        ) {
            val weight = weightRaw.replace(',', '.').toFloatOrNull()
            val reps = repsRaw.toIntOrNull()
            if (weight == null || reps == null) {
                _uiState.update { it.copy(message = "Gecerli agirlik ve tekrar girin.") }
                return
            }
            launchWithLoading {
                saveSetUseCase(workoutExerciseId, weight, reps).unwrapOrMessage("Set kaydedilemedi.")
            }
        }

        fun copyLastSet(workoutExerciseId: String) {
            launchWithLoading {
                copyLastSetUseCase(workoutExerciseId).unwrapOrMessage("Onceki set kopyalanamadi.")
            }
        }

        fun finishWorkout() {
            launchWithLoading {
                val summary =
                    finishWorkoutUseCase().getOrElse { error ->
                        _uiState.update { it.copy(message = error.message ?: "Antrenman bitirilemedi.") }
                        return@launchWithLoading
                    }
                _uiState.update {
                    it.copy(
                        lastFinishedSummary = summary,
                        message = "Antrenman tamamlandi.",
                        selectedWorkoutId = summary.workoutId,
                    )
                }
                observeDetail(summary.workoutId)
            }
        }

        fun selectHistoryWorkout(workoutId: String) {
            _uiState.update { it.copy(selectedWorkoutId = workoutId) }
            observeDetail(workoutId)
        }

        fun clearSelectedWorkout() {
            detailJob?.cancel()
            _uiState.update { it.copy(selectedWorkoutId = null, selectedWorkoutDetail = null) }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }

        private fun collectActiveWorkout() {
            viewModelScope.launch {
                observeActiveWorkoutUseCase()
                    .catch { error -> _uiState.update { it.copy(message = error.message ?: "Aktif antrenman yuklenemedi.") } }
                    .collect { active ->
                        _uiState.update { state -> state.copy(activeSession = active) }
                    }
            }
        }

        private fun collectHistory() {
            viewModelScope.launch {
                getWorkoutHistoryUseCase()
                    .catch { error -> _uiState.update { it.copy(message = error.message ?: "Gecmis yuklenemedi.") } }
                    .collect { history ->
                        _uiState.update { state -> state.copy(history = history) }
                    }
            }
        }

        private fun observeDetail(workoutId: String) {
            detailJob?.cancel()
            detailJob =
                viewModelScope.launch {
                    observeWorkoutDetailUseCase(workoutId)
                        .catch { error -> _uiState.update { it.copy(message = error.message ?: "Detay yuklenemedi.") } }
                        .collect { detail ->
                            _uiState.update { it.copy(selectedWorkoutDetail = detail) }
                        }
                }
        }

        private fun seedAndLoadExercises() {
            viewModelScope.launch {
                ensureExerciseCatalogSeededUseCase()
                onExerciseQueryChange("")
            }
        }

        private fun launchWithLoading(block: suspend () -> Unit) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, message = null) }
                runCatching { block() }
                    .onFailure { error -> _uiState.update { it.copy(message = error.message ?: "Beklenmeyen hata") } }
                _uiState.update { it.copy(isLoading = false) }
            }
        }

        private fun <T> Result<T>.unwrapOrMessage(defaultMessage: String): T {
            return getOrElse { error ->
                _uiState.update { it.copy(message = error.message ?: defaultMessage) }
                throw error
            }
        }
    }
