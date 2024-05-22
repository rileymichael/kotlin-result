plugins {
    `maven-publish`
    id("kotlin-conventions")
    id("publish-conventions")
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                compileOnly(libs.detekt.api)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.detekt.test)
                implementation(project(":kotlin-result"))
            }
        }
    }
}
