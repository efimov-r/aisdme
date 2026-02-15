package non.era.aisdme

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import non.era.aisdme.files.FileDolphinOpener
import non.era.aisdme.files.FilesController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.EditorHalfState
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.errors.GlobalMessage
import non.era.aisdme.ui.project.FileNode
import non.era.aisdme.ui.project.MoveController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.ProjectNode
import non.era.aisdme.ui.project.TypeClickNode

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}

class AppController(
    private val editorC: EditorController,
    private val projectC: ProjectController,
    private val filesC: FilesController,
    private val moveC: MoveController,
)  {
    private val _globalMess = mutableStateListOf<GlobalMessage>()
    val globalMess: List<GlobalMessage> get() = _globalMess

    init {
        editorC.subscriptionError { _globalMess += it }
        projectC.subscriptionError { _globalMess += it }
        filesC.subscriptionError { _globalMess += it }
        moveC.subscriptionError { _globalMess += it }
    }

    fun getDragState() = moveC.getDragState()
    fun getNodeBounds() = moveC.getNodeBounds()
    fun finishDrop() = moveC.finishDrop()

    fun openSelectedNodesInEditor(panel: Panels) = editorC.openNodes(projectC.getSelectedNodes(), panel)

    fun rightToLeftHalf() = editorC.rightToLeftHalf()

    fun getEditorHalfLeftController(): EditorHalfState = editorC.leftTabs
    fun getEditorHalfRightController(): EditorHalfState = editorC.rightTabs
    fun getSelectedNodes() = projectC.getSelectedNodes()
    fun getNodes() = projectC.getNodes()

    fun clickNode(node: ProjectNode, typeClick: TypeClickNode) = projectC.clickNode(node, typeClick)

    fun createFileInSelected(name: String) = filesC.createFile(projectC.getSelectedNodes(), name)
    fun createDirectoryInSelected(name: String) = filesC.createDirectory(projectC.getSelectedNodes(), name)
    fun deleteSelected() = filesC.delete(projectC.getSelectedNodes())
    fun move(src: ProjectNode, dst: ProjectNode) = filesC.move(src, dst) //todo сделать перемещение мышкой?
    fun copySelectedToClipboard(cut: Boolean) {
        val selected = projectC.getSelectedNodes()
        if(cut) selected.forEach { projectC.cutNoteMarkRecursive(it) }
        filesC.copyToClipboard(selected, cut)
    }
    fun pasteToSelectedNode() = filesC.pasteFromClipboard(projectC.getSelectedNodes())

    fun reloadSelectedDeerFromDisk() = projectC.reloadDeerFromDisk()
    fun openSelectedInFileManager() = FileDolphinOpener.openNodesInFileManager(projectC.getSelectedNodes())

    fun reselectNode(node: ProjectNode) = projectC.reselectNode(node)

    fun clearGlobalMess() {
        _globalMess.clear()
    }

    fun removeMess(id: Int) {
        _globalMess.removeIf { it.id == id }
    }

    fun dispose() {
        editorC.dispose()
        projectC.dispose()
        filesC.dispose()
        moveC.dispose()
    }
}