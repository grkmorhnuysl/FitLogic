package com.fitlogic.ai.core.data.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal object WorkoutSeedParser {
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val allowedMuscleGroups =
        setOf("Chest", "Back", "Shoulders", "Legs", "Arms", "Core", "Glutes", "Conditioning")
    private val allowedEquipments =
        setOf("Barbell", "Dumbbell", "Machine", "Cable", "Bodyweight", "Kettlebell", "Band")
    private val allowedDifficulty = setOf("Beginner", "Intermediate", "Advanced")

    fun parseAndValidate(rawJson: String): List<ExerciseSeedRecord> {
        val normalizedJson =
            rawJson
                .trimStart('\uFEFF', ' ', '\n', '\r', '\t')
                .let { text ->
                    val firstArrayIndex = text.indexOf('[')
                    if (firstArrayIndex > 0) text.substring(firstArrayIndex) else text
                }
        val records = jsonParser.decodeFromString<List<ExerciseSeedRecord>>(normalizedJson)
        require(records.isNotEmpty()) { "Egzersiz seed dosyasi bos olamaz." }

        val ids = records.map { it.id }
        require(ids.toSet().size == ids.size) { "Egzersiz seed dosyasinda duplicate id var." }

        records.forEach { record ->
            require(record.id.isNotBlank()) { "Egzersiz id bos olamaz." }
            require(record.name.isNotBlank()) { "Egzersiz adi bos olamaz. id=${record.id}" }
            require(record.instructions.isNotBlank()) { "Instructions bos olamaz. id=${record.id}" }
            require(record.instructionSteps.isNotEmpty()) { "Instruction steps bos olamaz. id=${record.id}" }
            require(record.commonMistakes.isNotEmpty()) { "Common mistakes bos olamaz. id=${record.id}" }
            require(record.gifAssetPath.isNotBlank()) { "Gif path bos olamaz. id=${record.id}" }
            require(record.muscleGroup in allowedMuscleGroups) {
                "Gecersiz muscleGroup '${record.muscleGroup}'. id=${record.id}"
            }
            require(record.equipment in allowedEquipments) {
                "Gecersiz equipment '${record.equipment}'. id=${record.id}"
            }
            require(record.difficulty in allowedDifficulty) {
                "Gecersiz difficulty '${record.difficulty}'. id=${record.id}"
            }
        }

        val idSet = ids.toSet()
        records.forEach { record ->
            record.alternativeExerciseIds.forEach { altId ->
                require(altId in idSet) {
                    "Alternative exercise id bulunamadi: '$altId' source='${record.id}'"
                }
            }
        }

        return records
    }
}

@Serializable
internal data class ExerciseSeedRecord(
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String,
    val instructionSteps: List<String>,
    val commonMistakes: List<String>,
    val alternativeExerciseIds: List<String>,
    val gifAssetPath: String,
)
