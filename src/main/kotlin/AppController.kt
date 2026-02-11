package non.era.aisdme

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import non.era.aisdme.files.FilesController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.EditorHalfState
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.errors.ErrorData
import non.era.aisdme.ui.project.DirNode
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.ProjectNode
import non.era.aisdme.ui.project.TreeState
import java.io.File
import java.nio.file.Path

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}

class AppController(
    private val editorC: EditorController,
    private val projectC: ProjectController,
    private val filesC: FilesController,
) {
    private val _errors = mutableStateListOf<ErrorData>()
    val errors: List<ErrorData> get() = _errors

    init {
        editorC.subscriptionError {  _errors += it }
        projectC.subscriptionError {  _errors += it }
        filesC.subscriptionError {  _errors += it }
    }

    fun openFileInEditor(file: File, panel: Panels) = editorC.openFile(file, panel)
    fun rightToLeftHalf() = editorC.rightToLeftHalf()

    fun getEditorHalfLeftController() : EditorHalfState = editorC.leftTabs
    fun getEditorHalfRightController() : EditorHalfState = editorC.rightTabs
    fun getProjectState() : TreeState = projectC

    fun createFile(path: Path) = filesC.createFile(path)
    fun createDirectory(path: Path) = filesC.createDirectory(path)
    fun delete(path: Path) = filesC.delete(path)
    fun deleteRecursive(path: Path) = filesC.deleteRecursive(path)
    fun copy(src: Path, dst: Path) = filesC.copy(src, dst)
    fun copyRecursive(src: Path, dst: Path) = filesC.copyRecursive(src, dst)
    fun move(src: Path, dst: Path) = filesC.move(src, dst)
    fun copyToClipboard(paths: List<Path>, cut: Boolean) = filesC.copyToClipboard(paths, cut)
    fun pasteFromClipboard(targetDir: Path) = filesC.pasteFromClipboard(targetDir)
    fun reloadDeerFromDisk(node: DirNode) = projectC.reloadDeerFromDisk(node)

    fun reselectElementTree(node: ProjectNode) {
        projectC.reselectMarkProjectNode(node)
    }
    fun cutNode(node: ProjectNode) {
        projectC.cutNoteMarkRecursive(node)
        filesC.copyToClipboard(listOf(node.file.toPath()), true)
    }

    fun clearErrors() {
        _errors.clear()
    }

    fun removeError(id: Int) {
        _errors.removeIf { it.id == id }
    }

    fun dispose() {
        editorC.dispose()
        projectC.dispose()
        filesC.dispose()
    }
}