package non.era.aisdme.configs

import kotlinx.coroutines.launch
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.ProjectOpen
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController

class ConfigController(
    private val bus: AppEventBus
) : UiController() {

    private var _project: ConfigStore<ProjectConfig>? = null
    val project: ConfigStore<ProjectConfig>?
    get() = _project

    init {
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is ProjectOpen -> {
                        _project = ConfigStore(
                            file = ConfigPaths.projectConfigFile(event.root.toPath()),
                            serializer = ProjectConfig.serializer(),
                            default = { ProjectConfig() },
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}