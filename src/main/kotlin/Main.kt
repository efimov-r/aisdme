package non.era.aisdme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.ui.AisDmeWindow
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.project.ProjectController
import non.era.aisdme.ui.project.ProjectTreeView
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
        AisDmeTheme {
            Row(Modifier.fillMaxSize()) {
                ProjectTreeView(
                    controller = projectController,
                    onFileOpen = { file ->
                        CoroutineScope(Dispatchers.IO).launch {
                            editorController.openFile(file)
                        }
                    },
                    modifier = Modifier.width(250.dp)
                )

                Divider(Modifier.fillMaxHeight().width(1.dp))

                Editor(editorController)
            }

        }
    }

    println("the end of main")
}

