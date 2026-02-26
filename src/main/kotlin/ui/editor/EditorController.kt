package non.era.aisdme.ui.editor

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
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

    var leftTabs = HalfEditorTabs(files, scope)
    var rightTabs = HalfEditorTabs(files, scope)

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
        removeOldFiles()
    }

    fun rightToLeftHalf() {
        leftTabs.closeAllTabs()
        leftTabs = rightTabs
        rightTabs = HalfEditorTabs(files, scope)
        removeOldFiles()
    }

    private fun removeOldFiles() {
        files.mapNotNull {
            if(!leftTabs.listTabs.contains(it.key) && !rightTabs.listTabs.contains(it.key))
                return@mapNotNull it.key
            return@mapNotNull null
        }.forEach { fid->
            scope.launch {
                files[fid]?.saveNow()
                withContext(Dispatchers.Swing) {
                    files.remove(fid)
                }
            }
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