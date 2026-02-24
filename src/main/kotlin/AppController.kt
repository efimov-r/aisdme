package non.era.aisdme

import androidx.compose.runtime.*
import non.era.aisdme.configs.ConfigController
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.files.FileDolphinOpener
import non.era.aisdme.files.FilesController
import non.era.aisdme.ui.components.dialogs.DialogsController
import non.era.aisdme.ui.components.dialogs.GlobalMessageReq
import non.era.aisdme.ui.components.dialogs.UiDialog
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.EditorHalfState
import non.era.aisdme.ui.editor.Panels
import non.era.aisdme.ui.errors.GlobalMessage
import non.era.aisdme.ui.move.MoveController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.TypeClickNode
import non.era.aisdme.ui.project.TypeMovementOnTree
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.ui.reload.TreeReloadWatcher
import java.io.File
import java.nio.file.Path

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}


//todo попробовать другую структуру
class AppController(
    private val editorC: EditorController,
    private val projectC: ProjectController,
    private val filesC: FilesController,
    private val moveC: MoveController,
    private val reloadWatcher: TreeReloadWatcher,
    private val configC: ConfigController,
    private val dialogC: DialogsController,
    private val startGlobalConfig: AppConfig
)  {
    private val _globalMess = mutableStateListOf<GlobalMessage>()
    val globalMess: List<GlobalMessage> get() = _globalMess
    var isDarkTheme by mutableStateOf(startGlobalConfig.isDarkTheme)

    init {
        editorC.subscriptionError { _globalMess += it }
        projectC.subscriptionError { _globalMess += it }
        filesC.subscriptionError { _globalMess += it }
        moveC.subscriptionError { _globalMess += it }
        reloadWatcher.subscriptionError { _globalMess += it }
        configC.subscriptionError { _globalMess += it }
        dialogC.subscriptionError { _globalMess += it }
    }

    fun reselectProject(root: File): ProjectConfig? { //открыть новый проект
        projectC.saveTreeStateToConfig(configC.getStore())
        val res = configC.reselectProject(root) //первый всегда!
        reloadWatcher.switchProject(root.toPath())
        projectC.reselectRootProject(configC.getStore(), root)
        return res
    }

    fun requestDialog(dialog: UiDialog) = dialogC.requestDialog(dialog)
    fun requestGlobalMessage(mess: GlobalMessageReq) = dialogC.requestGlobalMessage(mess)
    fun getMoveController() = moveC
    fun updateKeyValueProjectConfig(key: String, value: String) = configC.updateKeyValue(key, value)

    fun openSelectedNodesInEditor(panel: Panels) = editorC.openNodes(projectC.getSelectedNodes(), panel)

    fun rightToLeftHalf() = editorC.rightToLeftHalf()

    fun getEditorHalfLeftController(): EditorHalfState = editorC.leftTabs
    fun getEditorHalfRightController(): EditorHalfState = editorC.rightTabs
    fun getSelectedNodes() = projectC.getSelectedNodes()
    fun getNodes() = projectC.getNodes()

    fun clickNode(node: ProjectNode, typeClick: TypeClickNode) = projectC.clickNode(node, typeClick)
    fun movementOnTree(typeMovement: TypeMovementOnTree) = projectC.movementOnTree(typeMovement)

    fun createFileInSelected(name: String) = filesC.createFile(projectC.getSelectedNodes(), name)
    fun createDirectoryInSelected(name: String) = filesC.createDirectory(projectC.getSelectedNodes(), name)
    fun deleteSelected() = filesC.delete(projectC.getSelectedNodes())
    fun move(src: ProjectNode, dst: ProjectNode) = filesC.move(src, dst) //todo сделать перемещение мышкой
    fun copySelectedToClipboard(cut: Boolean) {
        val selected = projectC.getSelectedNodes()
        if(cut) selected.forEach { projectC.cutNoteMarkRecursive(it) }
        filesC.copyToClipboard(selected, cut)
    }
    fun pasteFromClipboard(targer: Path) = filesC.pasteFromClipboard(targer)

    fun reloadSelectedDeerFromDisk() = projectC.reloadDirFromDisk(configC.getStore())
    fun openSelectedInFileManager() = FileDolphinOpener.openNodesInFileManager(projectC.getSelectedNodes())

    fun clearGlobalMess() {
        _globalMess.clear()
    }

    fun removeMess(id: Int) {
        _globalMess.removeIf { it.id == id }
    }

    fun dispose() {
        projectC.saveTreeStateToConfig(configC.getStore())
        editorC.dispose()
        projectC.dispose()
        filesC.dispose()
        moveC.dispose()
        reloadWatcher.dispose()
        configC.dispose()
        dialogC.dispose()
    }
}