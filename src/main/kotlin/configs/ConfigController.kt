package non.era.aisdme.configs

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.SaveSnapshotEditor
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController
import java.io.File

class ConfigController(bus: AppEventBus) : UiController() {

    init {
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is SaveSnapshotEditor -> {
                        project?.update {
                            it.copy(
                                snapshotEditor = event.snapshot
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private var project: ConfigStore<ProjectConfig>? = null
    fun getStore() = project

    fun reselectProject(projectRoot: File): ProjectConfig? {
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
        return runBlocking {
            return@runBlocking project?.get()
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