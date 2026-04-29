package com.fitlogic.ai.feature.workout

import com.fitlogic.ai.core.domain.model.ActiveWorkoutSession
import com.fitlogic.ai.core.domain.model.ExerciseCatalogItem
import com.fitlogic.ai.core.domain.model.FinishedWorkoutSummary
import com.fitlogic.ai.core.domain.model.WorkoutDetail
import com.fitlogic.ai.core.domain.model.WorkoutHistoryEntry

data class WorkoutUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val activeSession: ActiveWorkoutSession? = null,
    val history: List<WorkoutHistoryEntry> = emptyList(),
    val selectedWorkoutId: String? = null,
    val selectedWorkoutDetail: WorkoutDetail? = null,
    val exerciseQuery: String = "",
    val exerciseResults: List<ExerciseCatalogItem> = emptyList(),
    val lastFinishedSummary: FinishedWorkoutSummary? = null,
)
