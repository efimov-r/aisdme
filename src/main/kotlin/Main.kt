package non.era.aisdme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.configs.ConfigController
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.files.FilesController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.AisDmeSpacing
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.ui.components.custom.ResizableColumn
import non.era.aisdme.ui.components.custom.ResizableRow
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.errors.view.ErrorOverlay
import non.era.aisdme.ui.move.MoveController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.view.ProjectTreeView
import non.era.aisdme.ui.reload.TreeReloadWatcher
import non.era.aisdme.ui.win.AisDmeWindow
import non.era.aisdme.utils.Log

fun main() = application {
    AisDmeWindow(onCloseRequest = ::exitApplication) { project->
        val bus = remember {
            AppEventBus()
        }

        val editorController = remember {
            EditorController(bus = bus)
        }

        val projectController = remember {
            ProjectController(bus)
        }

        val filesController = remember {
            FilesController()
        }

        val moveController = remember {
            MoveController()
        }

        val reloadWotcher = remember {
            TreeReloadWatcher(bus)
        }

        val configController = remember {
            ConfigController(bus)
        }

        val appController = remember {
            AppController(editorController,
                projectController,
                filesController,
                moveController,
                reloadWotcher,
                configController)
        }

        val configStart = remember {
            appController.reselectProject(project)
        }

        DisposableEffect(Unit) {
            onDispose {
                Log.d("DisposableEffect do")
                appController.dispose()
            }
        }

        CompositionLocalProvider(
            LocalAppController provides appController
        ) {
            AisDmeTheme(false) { //todo переключатель
                AisDmeContent(configStart)
            }
        }
    }

    Log.d("the end of main")
}

@Composable
fun AisDmeContent(conf: ProjectConfig?) {
    val colName = "ResizableColumnMainTerminalAndOther"
    val rowName = "ResizableRowMainTreeAndEditor"
    val app = LocalAppController.current
    Row(Modifier.fillMaxSize().background(colors.background).padding(AisDmeSpacing.s)) {
        ResizableColumn(Modifier.fillMaxHeight().weight(1f),
             conf?.keyValue?.get(colName)?.toFloat() ?: 250f,
            { app.updateKeyValueProjectConfig(colName, it.toString()) },
            {
                ResizableRow(Modifier.fillMaxSize(),
                    conf?.keyValue?.get(rowName)?.toFloat() ?: 250f,
                    { app.updateKeyValueProjectConfig(rowName, it.toString()) },
                    {
                        ProjectTreeView(Modifier.fillMaxSize().panel())
                    },
                    {
                        Editor(conf, Modifier.panel())
                    })
            },
            {
                Box(Modifier.fillMaxSize().panel())
            })
        Box(Modifier.fillMaxHeight().width(60.dp).padding(start = 6.dp).panel().padding(AisDmeSpacing.s))
    }
    ErrorOverlay()
}

@Composable
fun Modifier.panel(): Modifier {
    return background(colors.surface, AisDmeShapes.large)
}