pluginManagement {
    includeBuild("build-logic/convention")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FitLogic"

include(":app")
include(":core:designsystem")
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:ai")
include(":feature:onboarding")
include(":feature:auth")
include(":feature:home")
include(":feature:workout")
include(":feature:nutrition")
include(":feature:exercises")
include(":feature:stats")
include(":feature:coach")
include(":feature:profile")
