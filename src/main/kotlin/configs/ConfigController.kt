package non.era.aisdme.configs

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.ProjectOpen
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController
import java.io.File

class ConfigController : UiController {

    private var project: ConfigStore<ProjectConfig>

    fun getStore() = project

    constructor(bus: AppEventBus, projectStart: File) : super() {
        project = ConfigStore(
            file = ConfigPaths.projectConfigFile(projectStart.toPath()),
            serializer = ProjectConfig.serializer(),
            default = { ProjectConfig() },
        )
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is ProjectOpen -> {
                        project = ConfigStore(
                            file = ConfigPaths.projectConfigFile(event.root.toPath()),
                            serializer = ProjectConfig.serializer(),
                            default = { ProjectConfig() },
                        )

                        val old = GlobalConfig.store.get().lastOpenProject
                        if (old != event.root.path) {
                            GlobalConfig.store.update {
                                it.copy(
                                    lastOpenProject = event.root.path
                                )
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }


    override fun disposeBefore() {
        runBlocking {
            project.update { it }
        }
    }
}