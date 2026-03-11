package non.era.aisdme.configs.store

import java.nio.file.Path
import java.nio.file.Paths

object ConfigPaths {

    private const val APP_DIR = "AisDme"
    const val PROJECT_CONFIG_DIR = ".aisdme"

    fun globalConfigDir(): Path {
        val os = System.getProperty("os.name").lowercase()

        return when {
            os.contains("win") -> {
                val appData = System.getenv("APPDATA")
                    ?: error("APPDATA not found")
                Paths.get(appData, APP_DIR)
            }

            else -> { // Linux / Mac
                val home = System.getProperty("user.home")
                Paths.get(home, ".config", APP_DIR)
            }
        }
    }

    fun globalConfigFile(): Path =
        globalConfigDir().resolve("config.json")

    fun projectConfigFile(projectRoot: Path): Path =
        projectRoot.resolve(PROJECT_CONFIG_DIR).resolve("config.json")
}