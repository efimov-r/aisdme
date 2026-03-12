package non.era.aisdme.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.configs.ConfigController
import non.era.aisdme.configs.dataclass.AppConfig
import non.era.aisdme.configs.repositories.EditorStateRepository
import non.era.aisdme.configs.repositories.TreeStateRepository
import non.era.aisdme.files.work.FilesController
import non.era.aisdme.ui.components.dialogs.elements.DialogsController
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.move.MoveController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.reload.TreeReloadWatcher
import non.era.aisdme.ui.win.WindowState

@Composable
fun buildAppController(globalConf: AppConfig) : AppController {
    val bus = remember {
        AppEventBus()
    }

    val winState = remember { WindowState() }

    val editorStateRepository = remember { EditorStateRepository() }
    val treeStateRepository = remember { TreeStateRepository() }

    val editorController = remember {
        EditorController(bus, editorStateRepository, winState)
    }

    val projectController = remember {
        ProjectController(bus, treeStateRepository)
    }

    val filesController = remember {
        FilesController(bus)
    }

    val moveController = remember {
        MoveController()
    }

    val reloadWotcher = remember {
        TreeReloadWatcher(bus)
    }

    val configController = remember {
        ConfigController(editorStateRepository, treeStateRepository)
    }

    val dialogController = remember {
        DialogsController()
    }

    return remember {
        AppController(editorController,
            projectController,
            filesController,
            moveController,
            reloadWotcher,
            configController,
            dialogController,
            globalConf,
            winState)
    }
}