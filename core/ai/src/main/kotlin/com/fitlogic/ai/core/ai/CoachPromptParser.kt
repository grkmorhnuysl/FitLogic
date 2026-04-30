package com.fitlogic.ai.core.ai

internal object CoachPromptParser {
    fun extractUserMessage(prompt: String): String {
        val marker = "Kullanici mesaji:"
        val start = prompt.indexOf(marker)
        if (start == -1) return prompt.takeLast(MAX_FALLBACK_CHARS).trim()

        return prompt
            .substring(start + marker.length)
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() && !it.startsWith("Baglam:") }
            .orEmpty()
    }

    private const val MAX_FALLBACK_CHARS = 160
}
