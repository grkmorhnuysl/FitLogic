package com.fitlogic.ai.core.ai.prompt

import com.fitlogic.ai.core.ai.prompt.templates.PromptTemplates
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptBuilder
    @Inject
    constructor() {
        fun weeklyReport(summary: String): String = "${PromptTemplates.WEEKLY_REPORT}\nVeri:\n$summary"

        fun plateau(summary: String): String = "${PromptTemplates.PLATEAU_DETECTION}\nVeri:\n$summary"

        fun postWorkout(summary: String): String = "${PromptTemplates.POST_WORKOUT}\nVeri:\n$summary"

        fun nutrition(summary: String): String = "${PromptTemplates.NUTRITION_ANALYSIS}\nVeri:\n$summary"

        fun coachChat(
            message: String,
            context: String? = null,
        ): String =
            buildString {
                append(PromptTemplates.COACH_CHAT)
                append("\nCevap tarzi: WhatsApp mesajlasmasi gibi dogal, net, 2-4 cumle.")
                append("\nVaryasyon anahtari: ")
                append(System.currentTimeMillis().toString(36).takeLast(4))
                append("\nKullanici mesaji:\n")
                append(message.trim())
                if (!context.isNullOrBlank()) {
                    append("\nBaglam:\n")
                    append(context.trim())
                }
            }
    }
