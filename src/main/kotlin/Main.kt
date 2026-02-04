package non.era.aisdme

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.ui.AisDmeWindow
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.editor.EditorController
import java.io.File

fun main() = application {
    val editorController = remember {
        EditorController(File("test.asm"))
    }

    DisposableEffect(Unit) {
        onDispose {
            editorController.saveNow()
            editorController.dispose()
        }
    }

    AisDmeWindow(onCloseRequest = ::exitApplication) {
        AisDmeTheme {
            val state = editorController.state
            Editor(
                text = state.text,
                error = state.error,
                onTextChange = editorController::updateText
            )
        }
    }
}

