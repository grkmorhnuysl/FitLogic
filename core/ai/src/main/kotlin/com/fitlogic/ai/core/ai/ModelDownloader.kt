package com.fitlogic.ai.core.ai

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DownloadStatus {
    IDLE,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
}

data class DownloadState(
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val wifiOnly: Boolean = true,
)

@Singleton
class ModelDownloader
    @Inject
    constructor(
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        private val _state = MutableStateFlow(DownloadState())
        val state: StateFlow<DownloadState> = _state.asStateFlow()

        private var downloadJob: Job? = null

        fun start(wifiOnly: Boolean) {
            if (_state.value.status == DownloadStatus.DOWNLOADING) return
            _state.value = _state.value.copy(status = DownloadStatus.DOWNLOADING, wifiOnly = wifiOnly)
            downloadJob =
                CoroutineScope(dispatcher).launch {
                    while (_state.value.progress < 100 && _state.value.status == DownloadStatus.DOWNLOADING) {
                        delay(180)
                        _state.value = _state.value.copy(progress = (_state.value.progress + 4).coerceAtMost(100))
                    }
                    if (_state.value.progress >= 100) {
                        _state.value = _state.value.copy(status = DownloadStatus.COMPLETED)
                    }
                }
        }

        fun pause() {
            if (_state.value.status != DownloadStatus.DOWNLOADING) return
            downloadJob?.cancel()
            _state.value = _state.value.copy(status = DownloadStatus.PAUSED)
        }

        fun resume() {
            if (_state.value.status != DownloadStatus.PAUSED) return
            start(wifiOnly = _state.value.wifiOnly)
        }
    }
