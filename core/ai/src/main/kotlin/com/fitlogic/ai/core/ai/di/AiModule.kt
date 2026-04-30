package com.fitlogic.ai.core.ai.di

import com.fitlogic.ai.core.ai.AiEngine
import com.fitlogic.ai.core.ai.GemmaAiEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindAiEngine(impl: GemmaAiEngine): AiEngine
}
