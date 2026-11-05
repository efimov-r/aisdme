package non.era.aisdme.configs.store

import non.era.aisdme.platform.Platform
import java.nio.file.Path

object ConfigPaths {

    private const val APP_DIR = "AisDme"
    const val PROJECT_CONFIG_DIR = ".aisdme"

    fun globalConfigDir(): Path =
        Platform.current.configRoot().resolve(APP_DIR)

    fun globalConfigFile(): Path =
        globalConfigDir().resolve("config.json")

    fun projectConfigFile(projectRoot: Path): Path =
        projectRoot.resolve(PROJECT_CONFIG_DIR).resolve("config.json")
}