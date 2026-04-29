package com.fitlogic.ai.core.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitlogic.ai.core.data.local.FitLogicDatabase
import com.fitlogic.ai.core.data.local.entity.ExercisesCatalogEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExercisesCatalogDaoTest {
    private lateinit var database: FitLogicDatabase
    private lateinit var dao: ExercisesCatalogDao

    @Before
    fun setup() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FitLogicDatabase::class.java,
            ).allowMainThreadQueries()
                .build()
        dao = database.exercisesCatalogDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun search_appliesMuscleEquipmentAndDifficultyFilters() =
        runBlocking {
            dao.insertAll(
                listOf(
                    sampleExercise(
                        id = "ex-1",
                        name = "Bench Press",
                        muscleGroup = "Chest",
                        equipment = "Barbell",
                        difficulty = "Beginner",
                    ),
                    sampleExercise(
                        id = "ex-2",
                        name = "Incline Press",
                        muscleGroup = "Chest",
                        equipment = "Dumbbell",
                        difficulty = "Intermediate",
                    ),
                    sampleExercise(
                        id = "ex-3",
                        name = "Pull Up",
                        muscleGroup = "Back",
                        equipment = "Bodyweight",
                        difficulty = "Advanced",
                    ),
                ),
            )

            val results =
                dao.search(
                    query = "Press",
                    muscleGroups = listOf("Chest"),
                    equipments = listOf("Dumbbell"),
                    difficulty = "Intermediate",
                    limit = 20,
                )

            assertEquals(1, results.size)
            assertEquals("ex-2", results.first().id)
        }

    @Test
    fun search_withoutFilters_returnsAllMatchingByQuery() =
        runBlocking {
            dao.insertAll(
                listOf(
                    sampleExercise(id = "ex-1", name = "Bench Press"),
                    sampleExercise(id = "ex-2", name = "Shoulder Press"),
                ),
            )

            val results =
                dao.search(
                    query = "Press",
                    muscleGroups = listOf("__all__"),
                    equipments = listOf("__all__"),
                    difficulty = "",
                    limit = 20,
                )

            assertEquals(2, results.size)
            assertTrue(results.map { it.id }.containsAll(listOf("ex-1", "ex-2")))
        }

    private fun sampleExercise(
        id: String,
        name: String,
        muscleGroup: String = "Chest",
        equipment: String = "Barbell",
        difficulty: String = "Beginner",
    ): ExercisesCatalogEntity =
        ExercisesCatalogEntity(
            id = id,
            name = name,
            muscleGroup = muscleGroup,
            equipment = equipment,
            difficulty = difficulty,
            instructions = "$name aciklamasi",
            instructionStepsJson = """["adim-1","adim-2"]""",
            commonMistakesJson = """["hata-1"]""",
            alternativeExerciseIdsJson = """["ex-010"]""",
            gifAssetPath = "gifs/exercises/$id.gif",
        )
}
