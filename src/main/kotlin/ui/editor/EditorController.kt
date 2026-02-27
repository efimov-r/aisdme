package non.era.aisdme.ui.editor

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.bus.SaveSnapshotEditor
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    private val bus: AppEventBus
) : UiController() {
    private var files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()

    var leftTabs = HalfEditorTabs()
    var rightTabs = HalfEditorTabs()

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
        files.clear()
        snapshot?.let {snap->
            snap.files.forEach { id, filePath ->
                val fileState = ObservableOpenedFile.createFromConfig(id, filePath)
                files[id] = fileState
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
            files[tabEmpty.id] = tabEmpty
            activateTab(tabEmpty.id, panel)
            scope.launch {
                tabEmpty.openFile()
            }
        }
        removeOldFilesAndCreateSnapshot()
    }

    fun rightToLeftHalf() {
        leftTabs.closeAllTabs()
        leftTabs = rightTabs
        rightTabs = HalfEditorTabs()
        removeOldFilesAndCreateSnapshot()
    }

    private fun removeOldFilesAndCreateSnapshot() {
        val idsToDel = files.mapNotNull {
            if(!leftTabs.listTabs.contains(it.key) && !rightTabs.listTabs.contains(it.key)) {
                return@mapNotNull it.key
            }
            return@mapNotNull null
        }

        scope.launch {
            idsToDel.map { fid->
                scope.launch {
                    files[fid]?.saveNow()
                    withContext(Dispatchers.Swing) {
                        files.remove(fid)
                    }
                }
            }.joinAll()
            val snapshot = withContext(Dispatchers.Swing) {
                SnapshotEditor.createSnapshot(files, leftTabs, rightTabs)
            }
            bus.publish(SaveSnapshotEditor(snapshot))
        }
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