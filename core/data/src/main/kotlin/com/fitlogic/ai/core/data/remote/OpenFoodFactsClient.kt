package com.fitlogic.ai.core.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

data class OpenFoodFactsFood(
    val id: String,
    val name: String,
    val brandName: String?,
    val barcode: String?,
    val kcalPer100g: Float,
    val proteinPer100g: Float,
    val carbPer100g: Float,
    val fatPer100g: Float,
)

interface OpenFoodFactsClient {
    suspend fun lookupByBarcode(barcode: String): Result<OpenFoodFactsFood?>

    suspend fun searchFoods(
        query: String,
        limit: Int,
    ): Result<List<OpenFoodFactsFood>>
}

@Singleton
class OpenFoodFactsClientImpl
    @Inject
    constructor() : OpenFoodFactsClient {
        override suspend fun lookupByBarcode(barcode: String): Result<OpenFoodFactsFood?> =
            runCatching {
                if (barcode.isBlank()) return@runCatching null
                val safeBarcode = URLEncoder.encode(barcode.trim(), Charsets.UTF_8.name())
                val url = "$BASE_URL/api/v2/product/$safeBarcode.json"
                val payload = executeGet(url)
                val root =
                    runCatching { jsonParser.parseToJsonElement(payload).jsonObject }
                        .getOrElse { throw IllegalStateException("Sunucu yaniti okunamadi. Lutfen tekrar dene.") }
                val product = root["product"]?.jsonObject ?: return@runCatching null
                parseProduct(product)
            }

        override suspend fun searchFoods(
            query: String,
            limit: Int,
        ): Result<List<OpenFoodFactsFood>> =
            runCatching {
                if (query.isBlank()) return@runCatching emptyList()
                val encoded = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
                val url =
                    buildString {
                        append("$BASE_URL/cgi/search.pl")
                        append("?search_terms=$encoded")
                        append("&search_simple=1&action=process&json=1")
                        append("&page_size=$limit")
                    }
                val payload = executeGet(url)
                val root =
                    runCatching { jsonParser.parseToJsonElement(payload).jsonObject }
                        .getOrElse { throw IllegalStateException("Arama sonucu okunamadi. Lutfen tekrar dene.") }
                val products = root["products"]?.jsonArray.orEmpty()
                products.mapNotNull { parseProduct(it.jsonObject) }
            }

        private suspend fun executeGet(url: String): String =
            withContext(Dispatchers.IO) {
                val connection =
                    (URL(url).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 8_000
                        readTimeout = 8_000
                        setRequestProperty("User-Agent", USER_AGENT)
                    }
                try {
                    val code = connection.responseCode
                    check(code in 200..299) { "OpenFoodFacts hatasi: HTTP $code" }
                    connection.inputStream.bufferedReader().use { it.readText() }
                } finally {
                    connection.disconnect()
                }
            }

        private fun parseProduct(product: kotlinx.serialization.json.JsonObject): OpenFoodFactsFood? {
            val name = product["product_name"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
            if (name.isBlank()) return null

            val nutriments = product["nutriments"]?.jsonObject
            val kcal = nutriments.float("energy-kcal_100g") ?: nutriments.float("energy-kcal")
            val protein = nutriments.float("proteins_100g")
            val carb = nutriments.float("carbohydrates_100g")
            val fat = nutriments.float("fat_100g")

            return OpenFoodFactsFood(
                id =
                    product["id"]?.jsonPrimitive?.contentOrNull
                        ?: product["code"]?.jsonPrimitive?.contentOrNull
                        ?: name,
                name = name,
                brandName = product["brands"]?.jsonPrimitive?.contentOrNull,
                barcode = product["code"]?.jsonPrimitive?.contentOrNull,
                kcalPer100g = kcal ?: 0f,
                proteinPer100g = protein ?: 0f,
                carbPer100g = carb ?: 0f,
                fatPer100g = fat ?: 0f,
            )
        }

        private fun kotlinx.serialization.json.JsonObject?.float(key: String): Float? {
            val value = this?.get(key)?.jsonPrimitive?.contentOrNull ?: return null
            return value.toFloatOrNull()
        }

        companion object {
            private const val BASE_URL = "https://world.openfoodfacts.org"
            private const val USER_AGENT = "FitLogicAI/1.0 (fitlogic.local)"
            private val jsonParser = Json { ignoreUnknownKeys = true }
        }
    }
