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
    }
