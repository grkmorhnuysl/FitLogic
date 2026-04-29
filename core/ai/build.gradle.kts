plugins {
    id("fitlogic.android.library")
}

android {
    namespace = "com.fitlogic.ai.core.ai"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(libs.hilt.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
