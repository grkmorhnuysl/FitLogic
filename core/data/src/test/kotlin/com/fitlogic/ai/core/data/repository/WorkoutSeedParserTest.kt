package com.fitlogic.ai.core.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSeedParserTest {
    @Test
    fun parseAndValidate_success() {
        val result = WorkoutSeedParser.parseAndValidate(validSeedJson())
        assertEquals(2, result.size)
        assertEquals("ex-001", result.first().id)
    }

    @Test
    fun parseAndValidate_failsOnMissingField() {
        val broken =
            """
            [
              {
                "id": "ex-001",
                "name": "Bench Press",
                "muscleGroup": "Chest",
                "equipment": "Barbell",
                "difficulty": "Beginner",
                "instructions": "x",
                "instructionSteps": ["a"],
                "commonMistakes": ["b"],
                "alternativeExerciseIds": ["ex-002"]
              }
            ]
            """.trimIndent()

        val ex = runCatching { WorkoutSeedParser.parseAndValidate(broken) }.exceptionOrNull()
        assertTrue(ex != null)
    }

    @Test
    fun parseAndValidate_failsOnUnexpectedType() {
        val broken =
            """
            [
              {
                "id": "ex-001",
                "name": "Bench Press",
                "muscleGroup": "Chest",
                "equipment": "Barbell",
                "difficulty": "Beginner",
                "instructions": "x",
                "instructionSteps": "not-an-array",
                "commonMistakes": ["b"],
                "alternativeExerciseIds": ["ex-002"],
                "gifAssetPath": "gifs/exercises/ex-001.gif"
              }
            ]
            """.trimIndent()

        val ex = runCatching { WorkoutSeedParser.parseAndValidate(broken) }.exceptionOrNull()
        assertTrue(ex != null)
    }

    @Test
    fun parseAndValidate_failsOnUnknownAlternativeId() {
        val broken =
            """
            [
              {
                "id": "ex-001",
                "name": "Bench Press",
                "muscleGroup": "Chest",
                "equipment": "Barbell",
                "difficulty": "Beginner",
                "instructions": "x",
                "instructionSteps": ["a"],
                "commonMistakes": ["b"],
                "alternativeExerciseIds": ["ex-999"],
                "gifAssetPath": "gifs/exercises/ex-001.gif"
              }
            ]
            """.trimIndent()

        val ex = runCatching { WorkoutSeedParser.parseAndValidate(broken) }.exceptionOrNull()
        assertTrue(ex != null)
        assertTrue(ex!!.message!!.contains("Alternative exercise id bulunamadi"))
    }

    private fun validSeedJson(): String =
        """
        [
          {
            "id": "ex-001",
            "name": "Bench Press",
            "muscleGroup": "Chest",
            "equipment": "Barbell",
            "difficulty": "Beginner",
            "instructions": "x",
            "instructionSteps": ["a"],
            "commonMistakes": ["b"],
            "alternativeExerciseIds": ["ex-002"],
            "gifAssetPath": "gifs/exercises/ex-001.gif"
          },
          {
            "id": "ex-002",
            "name": "Pull Up",
            "muscleGroup": "Back",
            "equipment": "Bodyweight",
            "difficulty": "Intermediate",
            "instructions": "x",
            "instructionSteps": ["a"],
            "commonMistakes": ["b"],
            "alternativeExerciseIds": ["ex-001"],
            "gifAssetPath": "gifs/exercises/ex-002.gif"
          }
        ]
        """.trimIndent()
}
