package com.fitlogic.ai.core.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FitLogicDatabaseMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            FitLogicDatabase::class.java,
            emptyList(),
            FrameworkSQLiteOpenHelperFactory(),
        )

    @Test
    fun migrate1To2_preservesExistingUserAndAddsIsDeletedDefault() {
        val dbName = "migration-test.db"
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS `users` (
                  `id` TEXT NOT NULL,
                  `email` TEXT,
                  `display_name` TEXT NOT NULL,
                  `age` INTEGER,
                  `height_cm` REAL,
                  `weight_kg` REAL,
                  `gender` TEXT,
                  `activity_level` TEXT,
                  `goal_type` TEXT,
                  `target_calories` INTEGER,
                  `protein_grams` INTEGER,
                  `carb_grams` INTEGER,
                  `fat_grams` INTEGER,
                  `theme` TEXT NOT NULL,
                  `language` TEXT NOT NULL,
                  `weight_unit` TEXT NOT NULL,
                  `height_unit` TEXT NOT NULL,
                  `is_guest` INTEGER NOT NULL,
                  `created_at` INTEGER NOT NULL,
                  `updated_at` INTEGER NOT NULL,
                  `sync_status` TEXT NOT NULL,
                  PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO users(
                    id, email, display_name, age, height_cm, weight_kg, gender, activity_level, goal_type,
                    target_calories, protein_grams, carb_grams, fat_grams, theme, language, weight_unit,
                    height_unit, is_guest, created_at, updated_at, sync_status
                ) VALUES(
                    'user-1', 'user@example.com', 'User One', 28, 175.0, 70.0, 'MALE', 'MODERATE', 'MAINTAIN',
                    2200, 140, 240, 70, 'SYSTEM', 'TR', 'KG', 'CM', 0, 1000, 1000, 'SYNCED'
                )
                """.trimIndent(),
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(dbName, 2, true, FitLogicDatabase.MIGRATION_1_2)
        migratedDb.query("SELECT display_name, is_deleted FROM users WHERE id='user-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("User One", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
        }
    }

    @Test
    fun migrate3To4_addsExerciseCatalogDetailColumnsWithDefaults() {
        val dbName = "migration-test-3-4.db"
        helper.createDatabase(dbName, 3).apply {
            execSQL(
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
            execSQL(
                """
                INSERT INTO exercises_catalog(
                    id, name, muscle_group, equipment, difficulty, instructions
                ) VALUES(
                    'ex-legacy', 'Bench Press', 'Chest', 'Barbell', 'Beginner',
                    'Kontrollu tekrar ve stabilite'
                )
                """.trimIndent(),
            )
            close()
        }

        val migratedDb = helper.runMigrationsAndValidate(dbName, 4, false, FitLogicDatabase.MIGRATION_3_4)
        migratedDb
            .query(
                """
                SELECT instruction_steps_json, common_mistakes_json, alternative_exercise_ids_json, gif_asset_path
                FROM exercises_catalog
                WHERE id = 'ex-legacy'
                """.trimIndent(),
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("[]", cursor.getString(0))
                assertEquals("[]", cursor.getString(1))
                assertEquals("[]", cursor.getString(2))
                assertEquals("", cursor.getString(3))
            }
    }
}
