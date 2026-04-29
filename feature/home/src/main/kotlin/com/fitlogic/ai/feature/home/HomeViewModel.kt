package com.fitlogic.ai.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.usecase.gamification.CheckAchievementsUseCase
import com.fitlogic.ai.core.domain.usecase.gamification.ObserveAchievementsUseCase
import com.fitlogic.ai.core.domain.usecase.gamification.ObserveStreakUseCase
import com.fitlogic.ai.core.domain.usecase.gamification.ObserveWeeklyGoalUseCase
import com.fitlogic.ai.core.domain.usecase.gamification.SetWeeklyGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        observeStreakUseCase: ObserveStreakUseCase,
        observeWeeklyGoalUseCase: ObserveWeeklyGoalUseCase,
        observeAchievementsUseCase: ObserveAchievementsUseCase,
        private val checkAchievementsUseCase: CheckAchievementsUseCase,
        private val setWeeklyGoalUseCase: SetWeeklyGoalUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeUiState())
        val uiState = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                checkAchievementsUseCase()
            }
            viewModelScope.launch {
                combine(
                    observeStreakUseCase(),
                    observeWeeklyGoalUseCase(),
                    observeAchievementsUseCase(),
                ) { streak, weeklyGoal, achievements ->
                    HomeUiState(
                        isLoading = false,
                        streak = streak,
                        weeklyGoal = weeklyGoal,
                        achievements = achievements,
                        latestUnlockedAchievement = achievements.firstOrNull { it.unlockedAt != null },
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }

        fun setWeeklyGoal(target: Int) {
            viewModelScope.launch {
                setWeeklyGoalUseCase(target)
                    .onFailure { error ->
                        _uiState.update { it.copy(message = error.message ?: "Haftalik hedef guncellenemedi.") }
                    }
            }
        }

        fun clearMessage() {
            _uiState.update { it.copy(message = null) }
        }
    }
