plugins {
    id("fitlogic.android.library")
}

android {
    namespace = "com.fitlogic.ai.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
}
