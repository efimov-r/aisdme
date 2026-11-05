package non.era.aisdme.di

import androidx.compose.runtime.*
import non.era.aisdme.configs.ConfigController
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.files.work.FilesController
import non.era.aisdme.ui.components.dialogs.elements.DialogsController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.errors.GlobalMessages
import non.era.aisdme.ui.move.MoveController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.TreeActions
import non.era.aisdme.ui.reload.TreeReloadWatcher
import non.era.aisdme.ui.win.WindowState
import java.io.File

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}

class AppController(
    val editorController: EditorController,
    val projectController: ProjectController,
    val filesController: FilesController,
    val moveController: MoveController,
    private val reloadWatcher: TreeReloadWatcher,
    val configController: ConfigController,
    val dialogController: DialogsController,
    startGlobalConfig: AppConfig?,
    val winState: WindowState
)  { //корень композиции: контроллеры, их связи, открытие проекта и dispose
    val messages = GlobalMessages()
    val treeActions = TreeActions(projectController.treeState, editorController, filesController)

    var isDarkTheme by mutableStateOf(startGlobalConfig?.isDarkTheme ?: false)

    init {
        messages.collectFrom(
            editorController,
            projectController,
            filesController,
            moveController,
            reloadWatcher,
            configController,
            dialogController,
        )
        projectController.treeState.onDirsLoaded = reloadWatcher::dirsLoaded //напрямую: шина может потерять событие
    }

    suspend fun reselectProject(root: File): ProjectConfig { //открыть новый проект; вызывать с ui-потока
        winState.openProject(root.toPath())
        val res = configController.reselectProject(root) //первый всегда!
        reloadWatcher.switchProject(root.toPath())
        projectController.reselectRootProject(res.treeState, root)
        editorController.reselectProject(res.snapshotEditor)
        return res
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