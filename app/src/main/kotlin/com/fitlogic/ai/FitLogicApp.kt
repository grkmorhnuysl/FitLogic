package com.fitlogic.ai

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.fitlogic.ai.crash.CrashReporter
import com.fitlogic.ai.core.data.sync.SyncWorker
import com.fitlogic.ai.notification.FitLogicNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class FitLogicApp : Application(), Configuration.Provider {
    @Inject
    lateinit var notificationManager: FitLogicNotificationManager
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var crashReporter: CrashReporter

    override fun onCreate() {
        super.onCreate()
        notificationManager.ensureChannels()
        SyncWorker.enqueuePeriodic(this)
        crashReporter.log("FitLogicApp.onCreate")

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
