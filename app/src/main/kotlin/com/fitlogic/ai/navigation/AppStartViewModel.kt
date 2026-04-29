package com.fitlogic.ai.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlogic.ai.core.domain.model.AppEntryDestination
import com.fitlogic.ai.core.domain.usecase.user.ObserveEntryDestinationUseCase
import com.fitlogic.ai.core.domain.usecase.sync.TriggerInitialPullIfNeededUseCase
import com.fitlogic.ai.core.domain.usecase.sync.TriggerSyncNowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppStartViewModel
    @Inject
    constructor(
        private val observeEntryDestinationUseCase: ObserveEntryDestinationUseCase,
        private val triggerInitialPullIfNeededUseCase: TriggerInitialPullIfNeededUseCase,
        private val triggerSyncNowUseCase: TriggerSyncNowUseCase,
    ) : ViewModel() {
        private val _startRoute = MutableStateFlow(FitLogicRoute.Onboarding.route)
        val startRoute: StateFlow<String> = _startRoute.asStateFlow()

        init {
            viewModelScope.launch {
                observeEntryDestinationUseCase().collect { destination ->
                    _startRoute.value =
                        when (destination) {
                            AppEntryDestination.ONBOARDING -> FitLogicRoute.Onboarding.route
                            AppEntryDestination.AUTH -> FitLogicRoute.Auth.route
                            AppEntryDestination.HOME -> {
                                triggerInitialPullIfNeededUseCase()
                                triggerSyncNowUseCase()
                                FitLogicRoute.Home.route
                            }
                        }
                }
            }
        }
    }
