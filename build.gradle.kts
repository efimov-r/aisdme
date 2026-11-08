import java.awt.Color
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

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

val debugCtl = project.hasProperty("debugCtl") && !isRelease //gradlew run -PdebugCtl: пульт отладки в песочнице
val debugCtlDir = layout.buildDirectory.dir("debug-ctl")
val debugFixtureDir = layout.buildDirectory.dir("debug-fixture")

val prepareDebugFixture = tasks.register("prepareDebugFixture") { //свежий проект и конфиг: настоящие данные не трогаются
    outputs.upToDateWhen { false }

    doLast {
        val ctl = debugCtlDir.get().asFile
        val fixture = debugFixtureDir.get().asFile
        //старые ответы, снимки и конфиг — долой; команды (.cmd, .tmp) остаются: их могли положить, пока шла сборка
        ctl.listFiles()?.filter { it.extension != "cmd" && it.extension != "tmp" }?.forEach { it.deleteRecursively() }
        fixture.deleteRecursively()

        val proj = fixture.resolve("proj")
        mapOf(
            "main.asm" to "section .text\nglobal _start\n_start:\n    mov eax, 1\n    xor ebx, ebx\n    int 0x80",
            "notes.txt" to "line 1\nline 2\nline 3",
            "a.txt" to "a",
            "тест.txt" to "первая строка\nвторая строка",
            "fold/a.txt" to "a in fold",
            "fold/folda/Текстовый документ.txt" to "doc",
            "fold2/b.txt" to "b",
        ).forEach { (path, text) -> proj.resolve(path).apply { parentFile.mkdirs() }.writeText(text) }
        proj.resolve("fold/foldb").mkdirs() //пустая папка

        val img = BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until 64) for (y in 0 until 64) img.setRGB(x, y, Color(x * 4, y * 4, 128).rgb)
        ImageIO.write(img, "png", proj.resolve("pic.png"))

        val config = ctl.resolve("config").apply { mkdirs() }.resolve("config.json") //глобальный конфиг песочницы
        config.writeText("""{"lastOpenProject": "${proj.absolutePath.replace("\\", "\\\\")}"}""")
    }
}

compose.desktop {
    application {
        mainClass = "non.era.aisdme.MainKt"

        if (debugCtl) {
            jvmArgs += "-Daisdme.debug.dir=${debugCtlDir.get().asFile.absolutePath}"
            jvmArgs += "-Daisdme.debug.config=${debugCtlDir.get().asFile.resolve("config").absolutePath}"
        }
    }
}

tasks.matching { it.name == "run" }.configureEach {
    if (debugCtl) dependsOn(prepareDebugFixture)
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
                packages("non.era.aisdme.debug") //пульт отладки: нужно живое окно, решение пользователя
            }
        }

        verify {
            rule {
                minBound(65) //% строк; без разрешения пользователя не менять, см. AGENTS.md
            }
        }
    }
}