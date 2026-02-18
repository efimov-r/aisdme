package non.era.aisdme.configs

import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore

object GlobalConfig {
    val store = ConfigStore(
        file = ConfigPaths.globalConfigFile(),
        serializer = AppConfig.serializer(),
        default = { AppConfig() }
    )
}