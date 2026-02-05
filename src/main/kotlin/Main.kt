package non.era.aisdme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.AisDmeSpacing
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.ui.components.custom.ResizableColumn
import non.era.aisdme.ui.components.custom.ResizableRow
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.ProjectTreeView
import non.era.aisdme.ui.win.AisDmeWindow
import java.io.File

fun main() = application {
    val editorController = remember {
        EditorController()
    }

    val projectController = remember {
        ProjectController(File("/home/era/proj/os/aisdme/")).apply { load() }
    }

    DisposableEffect(Unit) {
        onDispose {
            editorController.saveNowAllFiles()
            editorController.dispose()
        }
    }

    AisDmeWindow(onCloseRequest = ::exitApplication) {
        AisDmeTheme(false) {
            AisDmeContent(editorController, projectController)
        }
    }

    println("the end of main")
}

@Composable
fun AisDmeContent(editorController: EditorController,
                  projectController: ProjectController) {
    ResizableColumn(Modifier.fillMaxSize().background(colors.background).padding(AisDmeSpacing.s),
        {
            ResizableRow(Modifier.fillMaxSize(),
                {
                    ProjectTreeView(
                        controller = projectController,
                        onFileOpen = { file ->
                            CoroutineScope(Dispatchers.IO).launch {
                                editorController.openFile(file)
                            }
                        },
                        modifier = Modifier.fillMaxSize().background(colors.surface, shape = AisDmeShapes.large)
                    )
                },
                {
                    Editor(editorController, Modifier.background(colors.surface, shape = AisDmeShapes.large).fillMaxSize())
                })
        },
        {
            Box(Modifier.fillMaxSize().background(colors.surface, shape = AisDmeShapes.large))
        })
}

