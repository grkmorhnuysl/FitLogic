package com.fitlogic.ai.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

interface CrashReporter {
    fun setUserId(userId: String)

    fun log(message: String)

    fun recordException(throwable: Throwable)
}

@Singleton
class FirebaseCrashReporter
    @Inject
    constructor() : CrashReporter {
        private val crashlytics: FirebaseCrashlytics? =
            runCatching { FirebaseCrashlytics.getInstance() }.getOrNull()

        override fun setUserId(userId: String) {
            crashlytics?.setUserId(userId)
        }

        override fun log(message: String) {
            crashlytics?.log(message)
        }

        override fun recordException(throwable: Throwable) {
            crashlytics?.recordException(throwable)
        }
    }
