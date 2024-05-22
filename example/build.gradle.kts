plugins {
    application
    kotlin("jvm")
    alias(libs.plugins.detekt)
}

application {
    mainClass.set("com.github.michaelbull.result.example.ApplicationKt")
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(project(":kotlin-result"))
    detektPlugins(project(":kotlin-result-detekt-rules"))
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.logback)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.netty)
}

detekt {
    config.setFrom(project.file("detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
}
