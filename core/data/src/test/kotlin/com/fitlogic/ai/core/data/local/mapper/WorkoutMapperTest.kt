package com.fitlogic.ai.core.data.local.mapper

import com.fitlogic.ai.core.data.local.entity.ExercisesCatalogEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutMapperTest {
    @Test
    fun exercisesCatalogEntity_toDomain_decodesJsonFields() {
        val entity =
            ExercisesCatalogEntity(
                id = "ex-001",
                name = "Bench Press",
                muscleGroup = "Chest",
                equipment = "Barbell",
                difficulty = "Beginner",
                instructions = "Aciklama",
                instructionStepsJson = """["adim-1","adim-2"]""",
                commonMistakesJson = """["hata-1","hata-2"]""",
                alternativeExerciseIdsJson = """["ex-010","ex-020"]""",
                gifAssetPath = "gifs/exercises/ex-001.gif",
            )

        val model = entity.toDomain()

        assertEquals(listOf("adim-1", "adim-2"), model.instructionSteps)
        assertEquals(listOf("hata-1", "hata-2"), model.commonMistakes)
        assertEquals(listOf("ex-010", "ex-020"), model.alternativeExerciseIds)
        assertEquals("gifs/exercises/ex-001.gif", model.gifAssetPath)
    }
}
