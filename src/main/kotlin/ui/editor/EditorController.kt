package non.era.aisdme.ui.editor

import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.bus.ShowFileInTree
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.dataclass.snapshots.SnapshotEditor
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.utils.Log
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

    fun getFile(id: Int) = files[id]

    fun updateTextAndAutosafeFile(id: Int, newText: String) {
        files[id]?.updateTextAndAutosave(newText, scope)
    }

    fun showSelectedInTree() {
        when {
            leftTabs.inFocus -> leftTabs.activeTabId
            rightTabs.inFocus -> rightTabs.activeTabId
            else -> null
        }?.let {  _files[it] }?.let { bus.publish(it.showFileInTreeEvent())}
    }

    fun reselectProject(snapshot: SnapshotEditor?) { //предпологаем что поток ui

        fun updateHalf(half: HalfEditorTabs, tabs: MutableList<Int>, sel: Int?) {
            var _sel = sel
            tabs.removeIf { !_files.keys.contains(it) }
            if(!tabs.contains(_sel)) _sel = null
            half.reselectProject(tabs, _sel ?: tabs.lastOrNull())
        }

        _files.clear()
        snapshot?.let { snap ->

            snap.files.forEach { id, filePath ->
                val fileState = ObservableOpenedFile.createFromConfig(id, filePath)
                _files[id] = fileState
                scope.launch {
                    fileState.openFile()
                }
            }
            updateHalf(leftTabs, snap.leftTabs.toMutableList(), snap.leftTabsSelect)
            updateHalf(rightTabs, snap.rightTabs.toMutableList(), snap.rightTabsSelect)
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