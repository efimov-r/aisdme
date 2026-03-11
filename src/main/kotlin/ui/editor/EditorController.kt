package non.era.aisdme.ui.editor

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.bus.*
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.editor.halfviewmodel.HalfViewModelFactory
import non.era.aisdme.ui.editor.halfviewmodel.ObservableOpenedHalf
import non.era.aisdme.ui.editor.halfviewmodel.ObservableOpenedHalfFile
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File
import java.nio.file.Path

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    private val bus: AppEventBus,
    private val rep: EditorStateRepository,
) : UiController() {
    private var _files: MutableMap<Int, ObservableOpenedHalf> = mutableMapOf()

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
                    is MoveFile -> {
                        getTabsFileFilter(event.oldFile).forEach { (_, file) ->
                            (file as ObservableOpenedHalfFile).refile(File(event.newFile.toString()))
                        }
                        saveToConfig()
                    }
                    is DeleteFile -> {
                        getTabsFileFilter(event.file).keys.forEach { id->
                            leftTabs.closeTab(id)
                            rightTabs.closeTab(id)
                        }
                    }
                    is DeleteDir -> {
                        getTabsFileFilter(event.dir, event.dir).keys.forEach { id->
                            leftTabs.closeTab(id)
                            rightTabs.closeTab(id)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun dropOutsideTree(src: List<ProjectNode>, position: Offset) {
        when {
            leftTabs.rect?.contains(position) == true -> Panels.LEFT
            rightTabs.rect?.contains(position) == true -> Panels.RIGHT
            else -> null
        }?.let { openNodes(src, it) }
    }

    fun dragPosition(position: Offset?) {
        if(position == null) {
            leftTabs.dragDest = false; rightTabs.dragDest = false
            return
        }
        leftTabs.dragDest = (leftTabs.rect?.contains(position) == true)
        rightTabs.dragDest = (rightTabs.rect?.contains(position) == true)
    }

    fun getFile(id: Int) = _files[id]

    fun showSelectedInTree() {
        when {
            leftTabs.inFocus -> leftTabs.activeTabId
            rightTabs.inFocus -> rightTabs.activeTabId
            else -> null
        }?.let { showInTree(it) }
    }

    fun showInTree(id: Int) {
        val file = _files[id]
        if(file is ObservableOpenedHalfFile) bus.publish(file.showFileInTreeEvent())
    }

    fun reselectProject(snapshot: SnapshotEditor?) { //предпологаем что поток ui

        fun updateHalf(half: HalfEditorTabs, tabs: MutableList<Int>, sel: Int?) {
            var _sel = sel
            tabs.removeIf { !_files.keys.contains(it) }
            if(!tabs.contains(_sel)) _sel = null
            half.reselectProject(tabs, _sel ?: tabs.lastOrNull())
        }

        _files.clear()
        HalfViewModelFactory.clearNexTabId()
        snapshot?.let { snap ->
            if(!snap.leftTabs.isEmpty() || !snap.rightTabs.isEmpty()) {
                snap.files.forEach { id, filePath ->
                    val fileState = HalfViewModelFactory.createObservableOpened(id, filePath, scope)
                    _files[id] = fileState
                    scope.launch {
                        fileState.openFile()
                    }
                }
                updateHalf(leftTabs, snap.leftTabs.toMutableList(), snap.leftTabsSelect)
                updateHalf(rightTabs, snap.rightTabs.toMutableList(), snap.rightTabsSelect)
            }
        }
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

    fun addFromCpipboardTab() : Int {
        val tab = HalfViewModelFactory.createObservableOpenedPaste(scope)
        tabToCash(tab)
        return tab.id
    }

    private fun openFile(file: File, panel: Panels = Panels.LEFT) {
        _files.values.find { it is ObservableOpenedHalfFile && it.compareFile(file) }?.id?.let { tabMapId ->
            activateTab(tabMapId, panel)
        } ?: run {
            val tab = HalfViewModelFactory.createObservableOpenedFile(file, scope)
            tabToCash(tab)
            activateTab(tab.id, panel)
        }
    }

    private fun tabToCash(tab: ObservableOpenedHalf) {
        _files[tab.id] = tab
        scope.launch {
            tab.openFile()
        }
        scope.launch {
            saveToConfig()
        }
    }

    private suspend fun saveToConfig() {
        rep.updateFiles( //уходит на сохранение в конфиг
            withContext(Dispatchers.Swing) {
                _files.mapValues { (_, value) ->
                    value.copy()
                }.toMap()
            }
        )
    }

    private fun getTabsFileFilter(eq: Path, startWith: Path? = null) =
        _files.filterValues {
            it is ObservableOpenedHalfFile &&
                    (it.getFile().toPath() == eq ||
                            if(startWith == null) false else it.getFile().path.startsWith(startWith.toString()))
        }

    fun rightToLeftHalf() {
        leftTabs = rightTabs.copy(updateLeftTabs, updateLeftActive)
        rightTabs = HalfEditorTabs(updateRightTabs, updateRightActive)
    }

    override fun disposeBefore() {
        runBlocking {
            _files.values.map { ta ->
                scope.launch {
                    ta.disposeBeforeBlocked()
                }
            }.joinAll()
        }
    }

    suspend fun removeNotUsedFiles() { //оставлю на всякий случай, вроде пока не нужно чистить кэшь
        withContext(Dispatchers.Swing) {
            _files.mapNotNull {
                if(!leftTabs.listTabs.contains(it.key) && !rightTabs.listTabs.contains(it.key)) {
                    return@mapNotNull it.key
                }
                return@mapNotNull null
            }
        }.map { fid->
            scope.launch {
                _files[fid]?.beforeRemove()
                withContext(Dispatchers.Swing) {
                    _files.remove(fid)
                }
            }
        }.joinAll()
    }
}