package non.era.aisdme.configs

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.dataclass.child.TreeStateConfig
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor
import non.era.aisdme.configs.repositories.TreeStateRepository
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController
import java.io.File

//todo вс епроверить
class ConfigController(
    private val editorRep: EditorStateRepository,
    private val treeRep: TreeStateRepository,
) : UiController() {
    private var project: ConfigStore<ProjectConfig>? = null
    fun getStore() = project //todo подумать может убрать

    private val latestEditorSnapshot = MutableStateFlow<SnapshotEditor?>(null)
    private var snapshotEditorJob: Job

    private val latestTreeSnapshot = MutableStateFlow<TreeStateConfig?>(null)
    private var snapshotTreeJob: Job

    init {
        snapshotEditorJob = scope.launch {
            combine(
                editorRep.files,
                editorRep.leftTabs,
                editorRep.rightTabs,
                editorRep.leftActive,
                editorRep.rightActive
            ) { files, leftTabs, rightTabs, leftActive, rightActive ->
                val trueFiles = files?.mapNotNull {
                    if(!it.value.writeStatus.toConfig) return@mapNotNull null
                    it.key to it.value.getFilePath()
                }?.toMap()
                val snapFromCash = project?.getFromCashe()?.snapshotEditor
                if(snapFromCash == null) {
                    if(trueFiles != null && leftTabs!= null && leftActive != null) {
                        val default = SnapshotEditor()
                        default.copy(
                            files = trueFiles,
                            leftTabs = leftTabs,
                            rightTabs = rightTabs ?: default.rightTabs,
                            leftTabsSelect = leftActive,
                            rightTabsSelect = rightActive ?: default.rightTabsSelect
                        )
                    } else {
                        null
                    }
                } else {
                    snapFromCash.copy(
                        files = trueFiles ?: snapFromCash.files,
                        leftTabs = leftTabs ?: snapFromCash.leftTabs,
                        rightTabs = rightTabs ?: snapFromCash.rightTabs,
                        leftTabsSelect = leftActive ?: snapFromCash.leftTabsSelect,
                        rightTabsSelect = rightActive ?: snapFromCash.rightTabsSelect
                    )
                }

            }
            .onEach { latestEditorSnapshot.value = it }   // кешируем изменения
            .debounce(4000)
            .collectLatest { snapshot ->
                snapshot?.let{ saveEditorSnapshot(it) }
            }
        }
        snapshotTreeJob = scope.launch {
            treeRep.tree
                .onEach { latestTreeSnapshot.value = it }
                .debounce { 2000 }
                .collectLatest { t->
                    t?.let { saveTreeSnapshot(it) }
            }
        }
    }

    private suspend fun saveEditorSnapshot(snapshot: SnapshotEditor) {
        project?.update {
            it.copy(snapshotEditor = snapshot)
        }
    }

    private suspend fun saveTreeSnapshot(snapshot: TreeStateConfig) {
        project?.update {
            it.copy(treeState = snapshot)
        }
    }

    fun reselectProject(projectRoot: File): ProjectConfig? {
        cancelUodateJobAndSaveNowBlocked()
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

    private fun cancelUodateJobAndSaveNowBlocked() {
        runBlocking {
            val j1 = scope.launch {
                snapshotEditorJob.cancelAndJoin()
                latestEditorSnapshot.value?.let {
                    saveEditorSnapshot(it)
                }
            }
            val j2 = scope.launch {
                snapshotTreeJob.cancelAndJoin()
                latestTreeSnapshot.value?.let {
                    saveTreeSnapshot(it)
                }
            }
            j1.join()
            j2.join()
        }
    }

    override fun disposeBefore() {
        cancelUodateJobAndSaveNowBlocked()
    }
}