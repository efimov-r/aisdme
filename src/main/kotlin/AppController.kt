package non.era.aisdme

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.files.FilesController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.EditorHalfState
import non.era.aisdme.ui.editor.Panels
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
    private val editor: EditorController,
    private val project: ProjectController,
    private val filesController: FilesController,
) {
    private val scopeFlow = CoroutineScope(SupervisorJob() + Dispatchers.Swing)
    private val _errors = mutableStateListOf<String>() //todo перейти на класс
    val errors: List<String> get() = _errors

    init {
        scopeFlow.launch {
            filesController.errors.collect { error ->
                _errors += error
            }
        }
    }

    fun openFileInEditor(file: File, panel: Panels) = editor.openFile(file, panel)
    fun rightToLeftHalf() = editor.rightToLeftHalf()

    fun getEditorHalfLeftController() : EditorHalfState = editor.leftTabs
    fun getEditorHalfRightController() : EditorHalfState = editor.rightTabs
    fun getProjectState() : TreeState = project

    fun createFile(path: Path) = filesController.createFile(path)
    fun createDirectory(path: Path) = filesController.createDirectory(path)
    fun delete(path: Path) = filesController.delete(path)
    fun deleteRecursive(path: Path) = filesController.deleteRecursive(path)
    fun copy(src: Path, dst: Path) = filesController.copy(src, dst)
    fun copyRecursive(src: Path, dst: Path) = filesController.copyRecursive(src, dst)
    fun move(src: Path, dst: Path) = filesController.move(src, dst)
    fun copyToClipboard(paths: List<Path>, cut: Boolean) = filesController.copyToClipboard(paths, cut)
    fun pasteFromClipboard(targetDir: Path) = filesController.pasteFromClipboard(targetDir)
    fun reloadDeerFromDisk(node: DirNode) = project.reloadDeerFromDisk(node)

    fun reselectElementTree(node: ProjectNode) {
        project.reselectMarkProjectNode(node)
    }
    fun cutNode(node: ProjectNode) {
        project.cutNoteMarkRecursive(node)
        filesController.copyToClipboard(listOf(node.file.toPath()), true)
    }

    fun clearErrors() {
        _errors.clear()
    }

    fun dispose() {
        scopeFlow.cancel()
        editor.dispose()
        project.dispose()
        filesController.dispose()
    }
}