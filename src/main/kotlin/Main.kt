package non.era.aisdme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

//todo CoroutineScope(Dispatchers.IO)
fun main() = application {
    val editorController = remember {
        EditorController()
    }

    val projectController = remember {
        ProjectController(File("/home/era/proj/os/aisdme/")).apply { load() }
    }

    DisposableEffect(Unit) {
        onDispose {
            println("DisposableEffect do")
            CoroutineScope(Dispatchers.IO).launch {
                editorController.saveAllAndDispose()
            }
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
    Row(Modifier.fillMaxSize().background(colors.background).padding(AisDmeSpacing.s)) {
        ResizableColumn(Modifier.fillMaxHeight().weight(1f),
            {
                ResizableRow(Modifier.fillMaxSize(),
                    {
                        ProjectTreeView(
                            controller = projectController,
                            onFileOpen = { file ->
                                editorController.openFile(file)
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
        Box(Modifier.fillMaxHeight().width(60.dp).padding(6.dp, 0.dp, 0.dp, 0.dp).background(colors.surface, shape = AisDmeShapes.large).padding(AisDmeSpacing.s))
    }

}

