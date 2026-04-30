package com.fitlogic.ai.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.fitlogic.ai.core.ai.AiEngine
import com.fitlogic.ai.core.ai.GemmaAiEngine
import com.fitlogic.ai.core.data.BuildConfig
import com.fitlogic.ai.core.data.local.FitLogicDatabase
import com.fitlogic.ai.core.data.local.dao.ExercisesCatalogDao
import com.fitlogic.ai.core.data.local.dao.AiInsightDao
import com.fitlogic.ai.core.data.local.dao.BodyWeightEntryDao
import com.fitlogic.ai.core.data.local.dao.AchievementDao
import com.fitlogic.ai.core.data.local.dao.FoodEntryDao
import com.fitlogic.ai.core.data.local.dao.FoodsCatalogDao
import com.fitlogic.ai.core.data.local.dao.NotificationSettingsDao
import com.fitlogic.ai.core.data.local.dao.SetDao
import com.fitlogic.ai.core.data.local.dao.UserDao
import com.fitlogic.ai.core.data.local.dao.WaterEntryDao
import com.fitlogic.ai.core.data.local.dao.WeeklyGoalDao
import com.fitlogic.ai.core.data.local.dao.WorkoutDao
import com.fitlogic.ai.core.data.local.dao.WorkoutExerciseDao
import com.fitlogic.ai.core.data.remote.AuthDataSource
import com.fitlogic.ai.core.data.remote.BarcodeScanDataSource
import com.fitlogic.ai.core.data.remote.MlKitBarcodeScanDataSource
import com.fitlogic.ai.core.data.remote.NutritionBarcodeGateway
import com.fitlogic.ai.core.data.remote.NutritionBarcodeGatewayImpl
import com.fitlogic.ai.core.data.remote.OpenFoodFactsClient
import com.fitlogic.ai.core.data.remote.OpenFoodFactsClientImpl
import com.fitlogic.ai.core.data.remote.SupabaseAuthDataSource
import com.fitlogic.ai.core.data.remote.SupabaseClientConfig
import com.fitlogic.ai.core.data.repository.NutritionRepositoryImpl
import com.fitlogic.ai.core.data.repository.AiInsightRepositoryImpl
import com.fitlogic.ai.core.data.repository.GamificationRepositoryImpl
import com.fitlogic.ai.core.data.repository.StatsRepositoryImpl
import com.fitlogic.ai.core.data.repository.SyncRepositoryImpl
import com.fitlogic.ai.core.data.repository.UserRepositoryImpl
import com.fitlogic.ai.core.data.repository.WorkoutRepositoryImpl
import com.fitlogic.ai.core.domain.repository.NutritionRepository
import com.fitlogic.ai.core.domain.repository.AiInsightRepository
import com.fitlogic.ai.core.domain.repository.GamificationRepository
import com.fitlogic.ai.core.domain.repository.StatsRepository
import com.fitlogic.ai.core.domain.repository.SyncRepository
import com.fitlogic.ai.core.domain.repository.UserRepository
import com.fitlogic.ai.core.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
object DataProvidersModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): FitLogicDatabase =
        Room.databaseBuilder(context, FitLogicDatabase::class.java, "fitlogic.db")
            .addMigrations(FitLogicDatabase.MIGRATION_1_2)
            .addMigrations(FitLogicDatabase.MIGRATION_2_3)
            .addMigrations(FitLogicDatabase.MIGRATION_3_4)
            .addMigrations(FitLogicDatabase.MIGRATION_4_5)
            .addMigrations(FitLogicDatabase.MIGRATION_5_6)
            .addMigrations(FitLogicDatabase.MIGRATION_6_7)
            .addMigrations(FitLogicDatabase.MIGRATION_7_8)
            .build()

    @Provides
    fun provideUserDao(database: FitLogicDatabase): UserDao = database.userDao()

    @Provides
    fun provideWorkoutDao(database: FitLogicDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideWorkoutExerciseDao(database: FitLogicDatabase): WorkoutExerciseDao = database.workoutExerciseDao()

    @Provides
    fun provideSetDao(database: FitLogicDatabase): SetDao = database.setDao()

    @Provides
    fun provideExercisesCatalogDao(database: FitLogicDatabase): ExercisesCatalogDao = database.exercisesCatalogDao()

    @Provides
    fun provideFoodsCatalogDao(database: FitLogicDatabase): FoodsCatalogDao = database.foodsCatalogDao()

    @Provides
    fun provideFoodEntryDao(database: FitLogicDatabase): FoodEntryDao = database.foodEntryDao()

    @Provides
    fun provideWaterEntryDao(database: FitLogicDatabase): WaterEntryDao = database.waterEntryDao()

    @Provides
    fun provideBodyWeightEntryDao(database: FitLogicDatabase): BodyWeightEntryDao = database.bodyWeightEntryDao()

    @Provides
    fun provideAiInsightDao(database: FitLogicDatabase): AiInsightDao = database.aiInsightDao()

    @Provides
    fun provideAchievementDao(database: FitLogicDatabase): AchievementDao = database.achievementDao()

    @Provides
    fun provideWeeklyGoalDao(database: FitLogicDatabase): WeeklyGoalDao = database.weeklyGoalDao()

    @Provides
    fun provideNotificationSettingsDao(database: FitLogicDatabase): NotificationSettingsDao = database.notificationSettingsDao()

    @Provides
    @Singleton
    fun provideSupabaseClientConfig(): SupabaseClientConfig =
        SupabaseClientConfig(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY,
            projectId = BuildConfig.SUPABASE_PROJECT_ID,
        )

    @Provides
    @Singleton
    fun provideAuthDataSource(impl: SupabaseAuthDataSource): AuthDataSource = impl

    @Provides
    @Singleton
    fun provideOpenFoodFactsClient(impl: OpenFoodFactsClientImpl): OpenFoodFactsClient = impl

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("session.preferences_pb") },
        )

    @Provides
    @Singleton
    fun provideAiEngine(impl: GemmaAiEngine): AiEngine = impl

}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    abstract fun bindNutritionRepository(impl: NutritionRepositoryImpl): NutritionRepository

    @Binds
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository

    @Binds
    abstract fun bindAiInsightRepository(impl: AiInsightRepositoryImpl): AiInsightRepository

    @Binds
    abstract fun bindGamificationRepository(impl: GamificationRepositoryImpl): GamificationRepository

    @Binds
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    abstract fun bindBarcodeScanDataSource(impl: MlKitBarcodeScanDataSource): BarcodeScanDataSource

    @Binds
    abstract fun bindNutritionBarcodeGateway(impl: NutritionBarcodeGatewayImpl): NutritionBarcodeGateway
}
