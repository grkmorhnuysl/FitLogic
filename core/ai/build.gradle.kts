import java.util.Properties

plugins {
    id("fitlogic.android.library")
}

val secretsProps =
    Properties().apply {
        val secretsFile = rootProject.file("secrets.properties")
        if (secretsFile.exists()) {
            secretsFile.inputStream().use { load(it) }
        }
    }

fun secretValue(
    key: String,
    defaultValue: String = "",
): String = (secretsProps.getProperty(key) ?: defaultValue).replace("\"", "\\\"")

android {
    namespace = "com.fitlogic.ai.core.ai"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "OPENAI_API_KEY", "\"${secretValue("OPENAI_API_KEY")}\"")
        buildConfigField("String", "OPENAI_BASE_URL", "\"${secretValue("OPENAI_BASE_URL", "https://api.openai.com/v1")}\"")
        buildConfigField("String", "OPENAI_MODEL", "\"${secretValue("OPENAI_MODEL", "gpt-4.1-mini")}\"")
        buildConfigField(
            "String",
            "LOCAL_LLM_MODEL_PATH",
            "\"${secretValue("LOCAL_LLM_MODEL_PATH", "/data/local/tmp/llm/gemma-2b-it.bin")}\"",
        )
        buildConfigField("Integer", "LOCAL_LLM_MAX_TOKENS", secretValue("LOCAL_LLM_MAX_TOKENS", "512"))
        buildConfigField("Integer", "LOCAL_LLM_MAX_TOP_K", secretValue("LOCAL_LLM_MAX_TOP_K", "64"))
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.mediapipe.tasks.genai)

    testImplementation(libs.junit4)
}
