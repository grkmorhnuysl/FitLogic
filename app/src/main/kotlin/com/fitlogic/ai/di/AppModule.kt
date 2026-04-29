package com.fitlogic.ai.di

import com.fitlogic.ai.crash.CrashReporter
import com.fitlogic.ai.crash.FirebaseCrashReporter
import com.fitlogic.ai.core.common.CoreCommonMarker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Named("app_bootstrap")
    fun provideAppBootstrapLabel(): String = CoreCommonMarker::class.simpleName ?: "CoreCommonMarker"

    @Provides
    @Singleton
    fun provideCrashReporter(): CrashReporter = FirebaseCrashReporter()
}
