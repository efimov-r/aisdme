plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.10"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.kotlinx.kover") version "0.9.11"
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

    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
    useJUnitPlatform()
}

detekt {
    buildUponDefaultConfig = true //свои правила поверх стандартных
    config.setFrom("config/detekt/detekt.yml")
    baseline = file("config/detekt/baseline.xml") //старые нарушения, новые роняют сборку
    parallel = true
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("androidx.compose.runtime.Composable") //UI модульными тестами не покрываем
                classes("*ComposableSingletons*", "non.era.aisdme.BuildConfig")
                packages("non.era.aisdme.theme")
            }
        }

        verify {
            rule {
                minBound(40) //% строк; только растёт, поднимать после новых тестов (#17)
            }
        }
    }
}