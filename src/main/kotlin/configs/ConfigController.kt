package non.era.aisdme.configs

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.dataclass.child.SnapshotTree
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
) : UiController() {
    private var projectStore: ConfigStore<ProjectConfig>? = null

    private val latestEditorSnapshot = MutableStateFlow<SnapshotEditor?>(null)
    private val latestTreeSnapshot = MutableStateFlow<SnapshotTree?>(null)

    init {
        scope.launch {
            combine(
                editorRep.files,
                editorRep.leftTabs,
                editorRep.rightTabs,
                editorRep.leftActive,
                editorRep.rightActive
            ) { files, leftTabs, rightTabs, leftActive, rightActive ->
                SnapshotEditor
                    .buildFromOldVersion(files, leftTabs, rightTabs, leftActive, rightActive, projectStore)
            }
            .onEach { latestEditorSnapshot.value = it }   // кешируем изменения
            .debounce(4000)
            .collectLatest { snapshot ->
                snapshot?.saveToConfig(projectStore)
            }
        }
        scope.launch {
            treeRep.tree
                .onEach { latestTreeSnapshot.value = it }
                .debounce { 2000 }
                .collectLatest { t->
                    t?.saveToConfig(projectStore)
            }
        }
    }

    fun reselectProject(projectRoot: File): ProjectConfig? {
        cancelUpdateJobAndSaveNowBlocked()
        projectStore = ConfigStore(
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
            return@runBlocking projectStore?.get()
        }
    }

    fun updateKeyValue(key: String, value: String) = scope.launch {
        projectStore?.update {
            it.copy(
                keyValue = it.keyValue.toMutableMap().apply { this[key] = value }.toMap()
            )
        }
    }

    private fun cancelUpdateJobAndSaveNowBlocked() = runBlocking {
        latestEditorSnapshot.value?.saveToConfig(projectStore)
        latestTreeSnapshot.value?.saveToConfig(projectStore)
    }

    override fun disposeBefore() {
        cancelUpdateJobAndSaveNowBlocked()
    }
}