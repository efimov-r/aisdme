package non.era.aisdme.configs

import kotlinx.coroutines.launch
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController
import java.io.File

class ConfigController(bus: AppEventBus) : UiController() {

    private var project: ConfigStore<ProjectConfig>? = null
    fun getStore() = project

    fun reselectProject(projectRoot: File) {
        project = ConfigStore(
            file = ConfigPaths.projectConfigFile(projectRoot.toPath()),
            serializer = ProjectConfig.serializer(),
            default = { ProjectConfig() },
        )
        scope.launch {
            val old = GlobalConfig.store.get().lastOpenProject
            if (old != projectRoot.path) {
                GlobalConfig.store.update {
                    it.copy(
                        lastOpenProject = projectRoot.path
                    )
                }
            }
        }
    }

    fun updateKeyValue(key: String, value: String) = scope.launch {
        project?.update {
            it.copy(
                keyValue = it.keyValue.toMutableMap().apply { this[key] = value }.toMap()
            )
        }
    }
}