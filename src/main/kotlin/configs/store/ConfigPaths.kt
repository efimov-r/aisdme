package non.era.aisdme.configs.store

import non.era.aisdme.BuildConfig
import non.era.aisdme.platform.Platform
import java.nio.file.Path

object ConfigPaths {

    private const val APP_DIR = "AisDme"
    const val PROJECT_CONFIG_DIR = ".aisdme"

    fun globalConfigDir(): Path = debugConfigDir() ?: Platform.current.configRoot().resolve(APP_DIR)

    //песочница пульта отладки (gradlew run -PdebugCtl): свой глобальный конфиг, настоящий не трогаем
    private fun debugConfigDir(): Path? =
        if (BuildConfig.DEBUG) System.getProperty("aisdme.debug.config")?.let { Path.of(it) } else null

    fun globalConfigFile(): Path =
        globalConfigDir().resolve("config.json")

    fun projectConfigFile(projectRoot: Path): Path =
        projectRoot.resolve(PROJECT_CONFIG_DIR).resolve("config.json")
}