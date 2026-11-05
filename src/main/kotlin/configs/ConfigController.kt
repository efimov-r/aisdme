package non.era.aisdme.configs

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.repositories.TreeStateRepository
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController
import java.io.File

class ConfigController(
    private val editorRep: EditorStateRepository,
    private val treeRep: TreeStateRepository,
    private val globalStore: ConfigStore<AppConfig> = GlobalConfig.store,
) : UiController() {
    private var projectStore: ConfigStore<ProjectConfig>? = null
    private var autosaveJobs: List<Job> = listOf()

    fun reselectProject(projectRoot: File): ProjectConfig? {
        saveNowAndStopBlocked() //снимки старого проекта — только в его конфиг
        editorRep.reset()
        treeRep.reset()

        val store = ConfigStore(
            file = ConfigPaths.projectConfigFile(projectRoot.toPath()),
            serializer = ProjectConfig.serializer(),
            default = { ProjectConfig() },
        )
        projectStore = store
        scope.launch {
            val old = globalStore.get().lastOpenProject
            if (old != projectRoot.path) {
                globalStore.update {
                    it.copy(
                        lastOpenProject = projectRoot.path
                    )
                }
            }
        }
        val res = runBlocking { store.get() }
        autosaveJobs = startAutosave(store)
        return res
    }

    fun updateKeyValue(key: String, value: String) {
        val store = projectStore ?: return
        scope.launch {
            store.update {
                it.copy(
                    keyValue = it.keyValue.toMutableMap().apply { this[key] = value }.toMap()
                )
            }
        }
    }

    //store фиксируется при запуске: отложенное сохранение не уйдёт в конфиг другого проекта
    private fun startAutosave(store: ConfigStore<ProjectConfig>) = listOf(
        scope.launch {
            combine(
                editorRep.files,
                editorRep.leftTabs,
                editorRep.rightTabs,
                editorRep.leftActive,
                editorRep.rightActive
            ) { files, leftTabs, rightTabs, leftActive, rightActive ->
                SnapshotEditor.buildFromOldVersion(files, leftTabs, rightTabs, leftActive, rightActive, store)
            }
            .debounce(4000)
            .collectLatest { snapshot ->
                snapshot?.saveToConfig(store)
            }
        },
        scope.launch {
            treeRep.tree
                .debounce(2000)
                .collectLatest { t->
                    t?.saveToConfig(store)
                }
        },
    )

    private fun saveNowAndStopBlocked() = runBlocking {
        autosaveJobs.forEach { it.cancelAndJoin() }
        autosaveJobs = listOf()
        val store = projectStore ?: return@runBlocking
        SnapshotEditor.buildFromOldVersion(
            editorRep.files.value,
            editorRep.leftTabs.value,
            editorRep.rightTabs.value,
            editorRep.leftActive.value,
            editorRep.rightActive.value,
            store,
        )?.saveToConfig(store)
        treeRep.tree.value?.saveToConfig(store)
    }

    override fun disposeBefore() {
        saveNowAndStopBlocked()
    }
}