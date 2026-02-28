package non.era.aisdme.configs

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.snapshots.SnapshotEditor
import non.era.aisdme.configs.store.ConfigPaths
import non.era.aisdme.configs.store.ConfigStore
import non.era.aisdme.ui.UiController
import java.io.File

//todo вс епроверить
class ConfigController(
    private val rep: EditorStateRepository,
) : UiController() {

    private val latestSnapshot = MutableStateFlow<SnapshotEditor?>(null)
    private var snapshotJob: Job

    init {
        snapshotJob = scope.launch {
            combine(
                rep.files,
                rep.leftTabs,
                rep.rightTabs,
                rep.leftActive,
                rep.rightActive
            ) { files, leftTabs, rightTabs, leftActive, rightActive ->
                val trueFailes = files?.mapNotNull {
                    if(!it.value.writeStatus.toConfig) return@mapNotNull null
                    it.key to it.value.getFilePath()
                }?.toMap()
                val oldSnap = project?.getFromCashe()?.snapshotEditor ?: SnapshotEditor()
                oldSnap.copy(
                    files = trueFailes ?: oldSnap.files,
                    leftTabs = leftTabs ?: oldSnap.leftTabs,
                    rightTabs = rightTabs ?: oldSnap.rightTabs,
                    leftTabsSelect = leftActive ?: oldSnap.leftTabsSelect,
                    rightTabsSelect = rightActive ?: oldSnap.rightTabsSelect
                )
            }
            .onEach { latestSnapshot.value = it }   // кешируем изменения
            .debounce(4000)
            .collectLatest { snapshot ->
                saveSnapshot(snapshot)
            }
        }
    }

    private suspend fun saveSnapshot(snapshot: SnapshotEditor) {
        project?.update {
            it.copy(snapshotEditor = snapshot)
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

    override fun disposeBefore() {
        runBlocking {
            snapshotJob.cancelAndJoin()
            latestSnapshot.value?.let {
                saveSnapshot(it)
            }
        }
    }
}