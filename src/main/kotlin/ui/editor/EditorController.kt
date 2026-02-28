package non.era.aisdme.ui.editor

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    private val bus: AppEventBus,
    private val rep: EditorStateRepository,
) : UiController() {
    private var _files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()
    private val files: Map<Int, ObservableOpenedFile> get() = _files

    private val updateLeftTabs: (List<Int>) -> Unit = { scope.launch { rep.updateLeftTabs(it) } }
    private val updateRightTabs: (List<Int>) -> Unit = { scope.launch { rep.updateRightTabs(it) } }
    private val updateLeftActive: (Int?) -> Unit = { scope.launch { rep.updateLeftActiveTabs(it) } }
    private val updateRightActive: (Int?) -> Unit = { scope.launch { rep.updateRightActiveTabs(it) } }

    var leftTabs = HalfEditorTabs( updateLeftTabs, updateLeftActive)
    var rightTabs = HalfEditorTabs(updateRightTabs, updateRightActive)

    init {
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is FileOpened -> {
                        withContext(Dispatchers.Swing) {
                            openFile(event.file)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun getFile(id: Int) = files[id]

    fun updateTextAndAutosafeFile(id: Int, newText: String) {
        files[id]?.updateTextAndAutosave(newText, scope)
    }

    fun reselectProject(snapshot: SnapshotEditor?) { //предпологаем что поток ui
        _files.clear()
        snapshot?.let {snap->
            snap.files.forEach { id, filePath ->
                val fileState = ObservableOpenedFile.createFromConfig(id, filePath)
                _files[id] = fileState
                scope.launch {
                    fileState.openFile()
                }
            }
        }
        leftTabs.reselectProject(snapshot?.leftTabs, snapshot?.leftTabsSelect)
        rightTabs.reselectProject(snapshot?.rightTabs, snapshot?.rightTabsSelect)
    }

    fun openNodes(nodes: List<ProjectNode>, panel: Panels = Panels.LEFT) {
        nodes.forEach { node->
            if(node is FileNode) openFile(node.getFile(), panel)
        }
    }

    private fun activateTab(id: Int, panel: Panels) = when(panel){
        Panels.RIGHT -> rightTabs.activateTab(id)
        Panels.LEFT -> leftTabs.activateTab(id)
    }

    private fun openFile(file: File, panel: Panels = Panels.LEFT) {
        files.values.find { it.compareFile(file) }?.id?.let { tabMapId ->
            activateTab(tabMapId, panel)
        } ?: run {
            val tabEmpty = ObservableOpenedFile.createEmpty(file)
            _files[tabEmpty.id] = tabEmpty
            activateTab(tabEmpty.id, panel)
            scope.launch {
                tabEmpty.openFile()
            }

            scope.launch {
                withContext(Dispatchers.Swing) {
                    files.mapNotNull {
                        if(!leftTabs.listTabs.contains(it.key) && !rightTabs.listTabs.contains(it.key)) {
                            return@mapNotNull it.key
                        }
                        return@mapNotNull null
                    }
                }.map { fid->
                    scope.launch {
                        files[fid]?.saveNow()
                        withContext(Dispatchers.Swing) {
                            _files.remove(fid)
                        }
                    }
                }.joinAll()

                rep.updateFiles(
                    withContext(Dispatchers.Swing) {
                        files.mapValues { (_, value) ->
                            value.copy()
                        }.toMap()
                    }
                )
            }
        }
    }

    fun rightToLeftHalf() {
        leftTabs.closeAllTabs()
        leftTabs = rightTabs.copy(updateLeftTabs, updateLeftActive)
        rightTabs = HalfEditorTabs(updateRightTabs, updateRightActive)
    }

    override fun disposeBefore() {
        runBlocking {
            files.values.map { ta ->
                scope.launch {
                    ta.saveTextOnDispose()
                }
            }.joinAll()
        }
    }
}