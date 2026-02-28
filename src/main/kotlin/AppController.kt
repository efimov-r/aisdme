package non.era.aisdme

import androidx.compose.runtime.*
import non.era.aisdme.configs.ConfigController
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.files.FilesController
import non.era.aisdme.ui.components.dialogs.elements.DialogsController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.errors.GlobalMessage
import non.era.aisdme.ui.move.MoveController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.reload.TreeReloadWatcher
import java.io.File

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}


//todo попробовать другую структуру
class AppController(
    val editorController: EditorController,
    val projectController: ProjectController,
    val filesController: FilesController,
    val moveController: MoveController,
    private val reloadWatcher: TreeReloadWatcher,
    val configController: ConfigController,
    val dialogController: DialogsController,
    startGlobalConfig: AppConfig?
)  {
    private val _globalMess = mutableStateListOf<GlobalMessage>()
    val globalMess: List<GlobalMessage> get() = _globalMess
    var isDarkTheme by mutableStateOf(startGlobalConfig?.isDarkTheme ?: false)

    init {
        editorController.subscriptionError { _globalMess += it }
        projectController.subscriptionError { _globalMess += it }
        filesController.subscriptionError { _globalMess += it }
        moveController.subscriptionError { _globalMess += it }
        reloadWatcher.subscriptionError { _globalMess += it }
        configController.subscriptionError { _globalMess += it }
        dialogController.subscriptionError { _globalMess += it }
    }

    fun reselectProject(root: File): ProjectConfig? { //открыть новый проект
        val res = configController.reselectProject(root) //первый всегда!
        reloadWatcher.switchProject(root.toPath())
        projectController.reselectRootProject(res?.treeState, root)
        editorController.reselectProject(res?.snapshotEditor)
        return res
    }
    fun openSelectedNodesInEditor(panel: Panels) = editorController.openNodes(projectController.getSelectedNodes(), panel)
    fun createFileInSelected(name: String) = filesController.createFile(projectController.getSelectedNodes(), name)
    fun createDirectoryInSelected(name: String) = filesController.createDirectory(projectController.getSelectedNodes(), name)
    fun deleteSelected() = filesController.delete(projectController.getSelectedNodes())
    fun copySelectedToClipboard(cut: Boolean) {
        val selected = projectController.getSelectedNodes()
        if(cut) selected.forEach { projectController.cutNoteMarkRecursive(it) }
        filesController.copyToClipboard(selected, cut)
    }

    fun clearGlobalMess() {
        _globalMess.clear()
    }

    fun removeMess(id: Int) {
        _globalMess.removeIf { it.id == id }
    }

    fun dispose() {
        editorController.dispose()
        projectController.dispose()
        filesController.dispose()
        moveController.dispose()
        reloadWatcher.dispose()
        configController.dispose()
        dialogController.dispose()
    }
}