package non.era.aisdme.ui.editor

import kotlinx.coroutines.launch
import non.era.aisdme.AppEventBus
import non.era.aisdme.FileOpened
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.FileNode
import non.era.aisdme.ui.project.ProjectNode
import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    val autosaveDelayMs: Long = 2000,
    private val bus: AppEventBus
) : UiController() {
    private var files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()

    var leftTabs = HalfEditorTabs(files, autosaveDelayMs, scope)
    var rightTabs = HalfEditorTabs(files, autosaveDelayMs, scope)

    init {
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is FileOpened -> openFile(event.file)
                }
            }
        }
    }


    fun openNodes(nodes: List<ProjectNode>, panel: Panels = Panels.LEFT) {
        nodes.forEach { node->
            if(node is FileNode) openFile(node.getFile(), panel)
        }
    }

    fun openFile(file: File, panel: Panels = Panels.LEFT) {
        when(panel){
            Panels.RIGHT -> rightTabs.openFile(file)
            Panels.LEFT -> leftTabs.openFile(file)
        }
    }

    fun rightToLeftHalf() {
        leftTabs.closeAllTabs()
        leftTabs = rightTabs
        rightTabs = HalfEditorTabs(files, autosaveDelayMs, scope)
    }

    override fun disposeAfter() {
        leftTabs.saveAll()
        rightTabs.saveAll()
    }
}