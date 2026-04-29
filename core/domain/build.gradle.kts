plugins {
    id("fitlogic.android.library")
}

android {
    namespace = "com.fitlogic.ai.core.domain"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
