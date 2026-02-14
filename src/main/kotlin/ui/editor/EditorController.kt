package non.era.aisdme.ui.editor

import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.FileNode
import non.era.aisdme.ui.project.ProjectNode
import java.io.File

enum class Panels{
    LEFT, RIGHT
}

class EditorController(
    val autosaveDelayMs: Long = 2000
) : UiController() {
    private var files: MutableMap<Int, ObservableOpenedFile> = mutableMapOf()

    var leftTabs = HalfEditorTabs(files, autosaveDelayMs, scope)
    var rightTabs = HalfEditorTabs(files, autosaveDelayMs, scope)

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