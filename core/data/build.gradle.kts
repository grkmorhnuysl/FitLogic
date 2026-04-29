import java.util.Properties

plugins {
    id("fitlogic.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.kapt")
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
): String {
    return (secretsProps.getProperty(key) ?: defaultValue).replace("\"", "\\\"")
}

android {
    namespace = "com.fitlogic.ai.core.data"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SUPABASE_URL", "\"${secretValue("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${secretValue("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "SUPABASE_PROJECT_ID", "\"${secretValue("SUPABASE_PROJECT_ID")}\"")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments +=
                    mapOf(
                        "room.schemaLocation" to file("schemas").path,
                        "room.incremental" to "true",
                        "room.expandProjection" to "true",
                    )
            }
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:ai"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.google.mlkit.barcode.scanning)

    testImplementation(libs.junit4)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
}
