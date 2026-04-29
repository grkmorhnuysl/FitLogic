@file:Suppress("MaxLineLength")

package com.fitlogic.ai.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitlogic.ai.core.data.local.dao.ExercisesCatalogDao
import com.fitlogic.ai.core.data.local.dao.FoodEntryDao
import com.fitlogic.ai.core.data.local.dao.FoodsCatalogDao
import com.fitlogic.ai.core.data.local.dao.SetDao
import com.fitlogic.ai.core.data.local.dao.UserDao
import com.fitlogic.ai.core.data.local.dao.WaterEntryDao
import com.fitlogic.ai.core.data.local.dao.WorkoutDao
import com.fitlogic.ai.core.data.local.dao.WorkoutExerciseDao
import com.fitlogic.ai.core.data.local.entity.ExercisesCatalogEntity
import com.fitlogic.ai.core.data.local.entity.FoodEntryEntity
import com.fitlogic.ai.core.data.local.entity.FoodsCatalogEntity
import com.fitlogic.ai.core.data.local.entity.SetEntity
import com.fitlogic.ai.core.data.local.entity.UserEntity
import com.fitlogic.ai.core.data.local.entity.WaterEntryEntity
import com.fitlogic.ai.core.data.local.entity.WorkoutEntity
import com.fitlogic.ai.core.data.local.entity.WorkoutExerciseEntity

@Database(
    entities = [
        UserEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        SetEntity::class,
        ExercisesCatalogEntity::class,
        FoodsCatalogEntity::class,
        FoodEntryEntity::class,
        WaterEntryEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class FitLogicDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun workoutDao(): WorkoutDao

    abstract fun workoutExerciseDao(): WorkoutExerciseDao

    abstract fun setDao(): SetDao

    abstract fun exercisesCatalogDao(): ExercisesCatalogDao

    abstract fun foodsCatalogDao(): FoodsCatalogDao

    abstract fun foodEntryDao(): FoodEntryDao

    abstract fun waterEntryDao(): WaterEntryDao

    companion object {
        val MIGRATION_1_2: Migration =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("ALTER TABLE users ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
                }
            }

        val MIGRATION_2_3: Migration =
            object : Migration(2, 3) {
                @Suppress("LongMethod")
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `workouts` (
                            `id` TEXT NOT NULL,
                            `user_id` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `status` TEXT NOT NULL,
                            `total_volume` REAL NOT NULL,
                            `started_at` INTEGER NOT NULL,
                            `finished_at` INTEGER,
                            `created_at` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            `sync_status` TEXT NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workouts_user_id_status` ON `workouts` (`user_id`, `status`)",
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_workouts_updated_at` ON `workouts` (`updated_at`)",
                    )

                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `workout_exercises` (
                            `id` TEXT NOT NULL,
                            `workout_id` TEXT NOT NULL,
                            `exercise_catalog_id` TEXT NOT NULL,
                            `exercise_name` TEXT NOT NULL,
                            `order_in_workout` INTEGER NOT NULL,
                            `created_at` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            `sync_status` TEXT NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS `index_workout_exercises_workout_id_order_in_workout`
                        ON `workout_exercises` (`workout_id`, `order_in_workout`)
                        """.trimIndent(),
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS `index_workout_exercises_exercise_catalog_id`
                        ON `workout_exercises` (`exercise_catalog_id`)
                        """.trimIndent(),
                    )

                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `sets` (
                            `id` TEXT NOT NULL,
                            `workout_id` TEXT NOT NULL,
                            `workout_exercise_id` TEXT NOT NULL,
                            `exercise_catalog_id` TEXT NOT NULL,
                            `weight_kg` REAL NOT NULL,
                            `reps` INTEGER NOT NULL,
                            `volume` REAL NOT NULL,
                            `is_pr` INTEGER NOT NULL,
                            `performed_at` INTEGER NOT NULL,
                            `created_at` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            `sync_status` TEXT NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_sets_workout_id` ON `sets` (`workout_id`)",
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS `index_sets_workout_exercise_id_performed_at`
                        ON `sets` (`workout_exercise_id`, `performed_at`)
                        """.trimIndent(),
                    )

                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `exercises_catalog` (
                            `id` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `muscle_group` TEXT NOT NULL,
                            `equipment` TEXT NOT NULL,
                            `difficulty` TEXT NOT NULL,
                            `instructions` TEXT NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                }
            }

        val MIGRATION_3_4: Migration =
            object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                        ALTER TABLE exercises_catalog
                        ADD COLUMN instruction_steps_json TEXT NOT NULL DEFAULT '[]'
                        """.trimIndent(),
                    )
                    database.execSQL(
                        """
                        ALTER TABLE exercises_catalog
                        ADD COLUMN common_mistakes_json TEXT NOT NULL DEFAULT '[]'
                        """.trimIndent(),
                    )
                    database.execSQL(
                        """
                        ALTER TABLE exercises_catalog
                        ADD COLUMN alternative_exercise_ids_json TEXT NOT NULL DEFAULT '[]'
                        """.trimIndent(),
                    )
                    database.execSQL(
                        """
                        ALTER TABLE exercises_catalog
                        ADD COLUMN gif_asset_path TEXT NOT NULL DEFAULT ''
                        """.trimIndent(),
                    )
                }
            }

        val MIGRATION_4_5: Migration =
            object : Migration(4, 5) {
                @Suppress("LongMethod")
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `foods_catalog` (
                            `id` TEXT NOT NULL,
                            `name` TEXT NOT NULL,
                            `brand_name` TEXT,
                            `barcode` TEXT,
                            `kcal_per_100g` REAL NOT NULL,
                            `protein_per_100g` REAL NOT NULL,
                            `carb_per_100g` REAL NOT NULL,
                            `fat_per_100g` REAL NOT NULL,
                            `source` TEXT NOT NULL,
                            `is_favorite` INTEGER NOT NULL,
                            `last_used_at` INTEGER,
                            `created_at` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            `sync_status` TEXT NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_foods_catalog_name` ON `foods_catalog` (`name`)",
                    )
                    database.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_foods_catalog_barcode` ON `foods_catalog` (`barcode`)",
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_foods_catalog_is_favorite` ON `foods_catalog` (`is_favorite`)",
                    )

                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `food_entries` (
                            `id` TEXT NOT NULL,
                            `user_id` TEXT NOT NULL,
                            `food_id` TEXT NOT NULL,
                            `food_name` TEXT NOT NULL,
                            `meal_type` TEXT NOT NULL,
                            `grams` REAL NOT NULL,
                            `kcal` REAL NOT NULL,
                            `protein` REAL NOT NULL,
                            `carb` REAL NOT NULL,
                            `fat` REAL NOT NULL,
                            `eaten_at` INTEGER NOT NULL,
                            `created_at` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            `sync_status` TEXT NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS `index_food_entries_user_id_eaten_at`
                        ON `food_entries` (`user_id`, `eaten_at`)
                        """.trimIndent(),
                    )
                    database.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_food_entries_food_id` ON `food_entries` (`food_id`)",
                    )

                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `water_entries` (
                            `id` TEXT NOT NULL,
                            `user_id` TEXT NOT NULL,
                            `amount_ml` INTEGER NOT NULL,
                            `consumed_at` INTEGER NOT NULL,
                            `created_at` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            `sync_status` TEXT NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent(),
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS `index_water_entries_user_id_consumed_at`
                        ON `water_entries` (`user_id`, `consumed_at`)
                        """.trimIndent(),
                    )
                }
            }
    }
}
