package com.fitlogic.ai.core.ai

import kotlin.math.absoluteValue

internal object CoachFallbackReplyBuilder {
    fun build(
        prompt: String,
        nonce: Int = System.nanoTime().toInt(),
    ): String {
        val userMessage = CoachPromptParser.extractUserMessage(prompt)
        val lower = userMessage.lowercase()
        val variant = (userMessage.hashCode() xor nonce).absoluteValue % VARIANT_COUNT
        val signature = userMessage.hashCode().toUInt().toString(16).takeLast(2)
        val focus =
            when {
                lower.contains("agri") ||
                    lower.contains("sakat") ||
                    lower.contains("yaralan") -> Focus.SAFETY
                lower.contains("beslen") ||
                    lower.contains("kalori") ||
                    lower.contains("protein") -> Focus.NUTRITION
                lower.contains("kilo") || lower.contains("zayif") || lower.contains("yag") -> Focus.WEIGHT
                lower.contains("bugun") || lower.contains("plan") -> Focus.TODAY_PLAN
                else -> Focus.GENERAL
            }

        return repliesFor(focus = focus, signature = signature)[variant.coerceIn(0, VARIANT_COUNT - 1)]
    }

    private fun repliesFor(
        focus: Focus,
        signature: String,
    ): List<String> =
        when (focus) {
            Focus.TODAY_PLAN ->
                listOf(
                    "Bugun sade gidelim: 8 dk isinma, 3 ana hareket, her birinde 3 set. Son sette 2 tekrar yedek kalsin. Imza:$signature",
                    "Bugun hedef teknik kalite: orta agirlik, kontrollu tempo, 30-40 dk. Bitiste 10 dk yuruyus ekle. Imza:$signature",
                    "Plan: once isin, sonra squat/itme/cekis gibi 3 temel hareket sec. Agirlik degil temiz form kazansin. Imza:$signature",
                )
            Focus.NUTRITION ->
                listOf(
                    "Bugun her ana ogune protein koy. Su hedefin 2L ustu olsun, atistirmayi tek porsiyonda tut. Imza:$signature",
                    "Beslenmede bugunun kuralini basit tut: protein once, sebze yanina, tatliyi planli porsiyonla sinirla. Imza:$signature",
                    "Kalori takibi zorsa tabak sistemi kullan: yarim tabak sebze, ceyrek protein, ceyrek karbonhidrat. Imza:$signature",
                )
            Focus.SAFETY ->
                listOf(
                    "Agri varsa bugun zorlamiyoruz. Hareket acisini kisalt, agirligi dusur, agri artarsa antrenmani kes. Imza:$signature",
                    "Bugun teknik gunu yap: hafif kilo, yavas tekrar. Agri yapan hareket yerine rahat varyasyon sec. Imza:$signature",
                    "Keskin agri normal degil. O bolgeyi yormadan yuruyus veya mobilite sec; devam ederse uzmana danis. Imza:$signature",
                )
            Focus.WEIGHT ->
                listOf(
                    "Yag kaybi icin bugun 8-10 bin adim, 30 dk kuvvet ve proteinli iki ana ogun yeterli bir hedef. Imza:$signature",
                    "Bugun buyuk hedef yok: porsiyonu azicik kucult, hareketi artir, gece atistirmasini kapat. Imza:$signature",
                    "Kilo hedefinde kazanacagin yer istikrar. Haftalik ortalamaya bak; bugun adim ve protein isini bitir. Imza:$signature",
                )
            Focus.GENERAL ->
                listOf(
                    "Ritmi koru: bugun orta tempoda 30 dk hareket, temiz form ve erken uyku. Kucuk ama tamamlanan hedef. Imza:$signature",
                    "Bugun tek odak sec: ya antrenman ya yuruyus ya da beslenme. Birini iyi yapman yeter. Imza:$signature",
                    "Devam hissi onemli. Kisa bir seans yap, bitince not al: ne kolaydi, neyi yarin artirabiliriz? Imza:$signature",
                )
        }

    private enum class Focus {
        TODAY_PLAN,
        NUTRITION,
        SAFETY,
        WEIGHT,
        GENERAL,
    }

    private const val VARIANT_COUNT = 3
}
