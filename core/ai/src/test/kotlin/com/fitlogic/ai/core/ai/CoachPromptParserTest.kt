package com.fitlogic.ai.core.ai

import com.fitlogic.ai.core.ai.prompt.PromptBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CoachPromptParserTest {
    @Test
    fun `extracts user message after marker newline`() {
        val prompt = PromptBuilder().coachChat("Bugun protein hedefim ne olmali?")

        assertEquals(
            "Bugun protein hedefim ne olmali?",
            CoachPromptParser.extractUserMessage(prompt),
        )
    }

    @Test
    fun `extracts first non blank user message line`() {
        val prompt =
            """
            Sistem talimati
            Kullanici mesaji:

              Agri varken bench press yapayim mi?
            Baglam:
            son antrenman
            """.trimIndent()

        assertEquals(
            "Agri varken bench press yapayim mi?",
            CoachPromptParser.extractUserMessage(prompt),
        )
    }

    @Test
    fun `different coach prompts keep different user messages`() {
        val nutritionPrompt = PromptBuilder().coachChat("Protein ve kalori nasil ayarlanmali?")
        val injuryPrompt = PromptBuilder().coachChat("Dizimde agri var, bugun ne yapayim?")

        assertNotEquals(
            CoachPromptParser.extractUserMessage(nutritionPrompt),
            CoachPromptParser.extractUserMessage(injuryPrompt),
        )
    }
}
