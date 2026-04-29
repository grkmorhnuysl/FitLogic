plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.4.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.0")
}

gradlePlugin {
    plugins {
        create("androidApplicationConvention") {
            id = "fitlogic.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        create("androidLibraryConvention") {
            id = "fitlogic.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        create("androidFeatureConvention") {
            id = "fitlogic.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        create("androidComposeConvention") {
            id = "fitlogic.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
    }
}

kotlin {
    jvmToolchain(17)
}
