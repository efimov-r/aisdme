package non.era.aisdme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import non.era.aisdme.files.FilesController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.AisDmeSpacing
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.ui.components.custom.ResizableColumn
import non.era.aisdme.ui.components.custom.ResizableRow
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.errors.ErrorOverlay
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.ProjectTreeView
import non.era.aisdme.ui.win.AisDmeWindow
import java.io.File

fun main() = application {
    AisDmeWindow(onCloseRequest = ::exitApplication) {
        val editorController = remember {
            EditorController()
        }

        val projectController = remember {
            ProjectController(File("/home/era/proj/os/aisdme/"))
        }

        val filesController = remember {
            FilesController()
        }

        val appController = remember {
            AppController(editorController, projectController, filesController)
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
                AisDmeContent()
            }
        }
    }

    Log.d("the end of main")
}

@Composable
fun AisDmeContent() {
    Row(Modifier.fillMaxSize().background(colors.background).padding(AisDmeSpacing.s)) {
        ResizableColumn(Modifier.fillMaxHeight().weight(1f),
            {
                ResizableRow(Modifier.fillMaxSize(),
                    {
                        ProjectTreeView(Modifier.fillMaxSize().panel())
                    },
                    {
                        Editor(Modifier.panel())
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