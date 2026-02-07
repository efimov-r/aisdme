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
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.AisDmeSpacing
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.ui.components.custom.ResizableColumn
import non.era.aisdme.ui.components.custom.ResizableRow
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.Panels
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
            ProjectController(File("/home/era/proj/os/aisdme/")).apply { load() }
        }

        val appController = remember {
            AppController(editorController, projectController)
        }

        DisposableEffect(Unit) {
            onDispose {
                println("DisposableEffect do")
                editorController.saveAllAndDispose()
            }
        }

        CompositionLocalProvider(
            LocalAppController provides appController
        ) {
            AisDmeTheme(false) {
                AisDmeContent()
            }
        }
    }

    println("the end of main")
}

@Composable
fun AisDmeContent() {
    val app = LocalAppController.current
    Row(Modifier.fillMaxSize().background(colors.background).padding(AisDmeSpacing.s)) {
        ResizableColumn(Modifier.fillMaxHeight().weight(1f),
            {
                ResizableRow(Modifier.fillMaxSize(),
                    {
                        ProjectTreeView(
                            state = app.getProjectState(),
                            onFileOpen = { file ->
                                app.openFile(file, Panels.LEFT)
                            },
                            modifier = Modifier.fillMaxSize().panel()
                        )
                    },
                    {
                        Editor(Modifier.panel().fillMaxSize())
                    })
            },
            {
                Box(Modifier.fillMaxSize().panel())
            })
        Box(Modifier.fillMaxHeight().width(60.dp).padding(6.dp, 0.dp, 0.dp, 0.dp).panel().padding(AisDmeSpacing.s))
    }

}

@Composable
fun Modifier.panel(): Modifier {
    return background(colors.surface, AisDmeShapes.large)
}