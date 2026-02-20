plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.10"
}

group = "non.era.aisdme"
version = "1.0-SNAPSHOT"

val isRelease = project.hasProperty("release")

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

val generateBuildConfig = tasks.register("generateBuildConfig") {
    val outputDir = layout.buildDirectory.dir("generated/buildconfig")
    outputs.dir(outputDir)
    outputs.upToDateWhen { false } // Всегда пересоздавать

    doLast {
        val file = outputDir.get().file("non/era/aisdme/BuildConfig.kt").asFile
        file.parentFile.mkdirs()

        file.writeText(
            """
            package non.era.aisdme

            object BuildConfig {
                const val DEBUG = ${!project.hasProperty("release")}
            }
            """.trimIndent()
        )

        println("✅ BuildConfig generated (DEBUG = ${!project.hasProperty("release")})")
    }
}

kotlin {
    jvmToolchain(17)

    sourceSets.main {
        kotlin.srcDir(layout.buildDirectory.dir("generated/buildconfig"))
    }
}

compose.desktop {
    application {
        mainClass = "non.era.aisdme.MainKt"
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateBuildConfig)
}