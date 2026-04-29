plugins {
    id("fitlogic.android.library")
}

android {
    namespace = "com.fitlogic.ai.core.ai"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
}
