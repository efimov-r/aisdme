package non.era.aisdme.di

import androidx.compose.runtime.staticCompositionLocalOf
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.configs.ConfigController
import non.era.aisdme.configs.GlobalConfig
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.repositories.TreeStateRepository
import non.era.aisdme.files.work.FilesController
import non.era.aisdme.settings.ThemeController
import non.era.aisdme.ui.components.dialogs.elements.DialogsController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.errors.GlobalMessages
import non.era.aisdme.ui.move.MoveController
import non.era.aisdme.ui.project.CurrentProject
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.TreeActions
import non.era.aisdme.ui.reload.TreeReloadWatcher
import java.io.File

val LocalAppController = staticCompositionLocalOf<AppController> {
    error("AppController not provided")
}

class AppController(startGlobalConfig: AppConfig?) { //корень композиции: создаёт контроллеры и связывает их
    private val bus = AppEventBus()
    private val editorStateRepository = EditorStateRepository()
    private val treeStateRepository = TreeStateRepository()

    val currentProject = CurrentProject()
    val editorController = EditorController(bus, editorStateRepository, currentProject)
    val projectController = ProjectController(bus, treeStateRepository)
    val filesController = FilesController(bus)
    val moveController = MoveController()
    private val reloadWatcher = TreeReloadWatcher(bus)
    val configController = ConfigController(editorStateRepository, treeStateRepository)
    val dialogController = DialogsController { projectController.treeState.getSelectedNodes() }
    val themeController = ThemeController(startGlobalConfig?.isDarkTheme ?: false, GlobalConfig.store)

    val messages = GlobalMessages()
    val treeActions = TreeActions(projectController.treeState, editorController, filesController)

    init {
        messages.collectFrom(
            editorController,
            projectController,
            filesController,
            moveController,
            reloadWatcher,
            configController,
            dialogController,
            themeController,
        )
        projectController.treeState.onDirsLoaded = reloadWatcher::dirsLoaded //напрямую: шина может потерять событие
    }

    suspend fun reselectProject(root: File): ProjectConfig { //открыть новый проект; вызывать с ui-потока
        currentProject.open(root.toPath())
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
        themeController.dispose()
    }
}