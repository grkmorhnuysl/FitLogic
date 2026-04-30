package com.fitlogic.ai.core.ai

import com.fitlogic.ai.core.ai.prompt.PromptBuilder
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachFallbackReplyBuilderTest {
    @Test
    fun `nutrition message does not fall back to generic reply`() {
        val prompt = PromptBuilder().coachChat("Protein ve kalori nasil ayarlanmali?")
        val reply = CoachFallbackReplyBuilder.build(prompt = prompt, nonce = 0)

        assertTrue(reply.contains("protein", ignoreCase = true) || reply.contains("kalori", ignoreCase = true))
    }

    @Test
    fun `pain message uses safety reply`() {
        val prompt = PromptBuilder().coachChat("Dizimde agri var, bugun ne yapayim?")
        val reply = CoachFallbackReplyBuilder.build(prompt = prompt, nonce = 0)

        assertTrue(reply.contains("Agri", ignoreCase = true) || reply.contains("zorlam", ignoreCase = true))
    }

    @Test
    fun `different user intents produce different replies`() {
        val nutritionReply =
            CoachFallbackReplyBuilder.build(
                prompt = PromptBuilder().coachChat("Protein ve kalori nasil ayarlanmali?"),
                nonce = 0,
            )
        val planReply =
            CoachFallbackReplyBuilder.build(
                prompt = PromptBuilder().coachChat("Bugun nasil bir antrenman plani yapayim?"),
                nonce = 0,
            )

        assertNotEquals(nutritionReply, planReply)
    }
}
