pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kotlin-result"

include(
    "benchmarks",
    "example",
    "kotlin-result",
    "kotlin-result-coroutines",
    "kotlin-result-detekt-rules",
)
