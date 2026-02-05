package non.era.aisdme

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.ui.AisDmeWindow
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.editor.EditorController
import non.era.aisdme.ui.editor.TabsBar
import java.io.File

fun main() = application {
    val editorController = remember {
        EditorController()
    }

    DisposableEffect(Unit) {
        onDispose {
            editorController.saveNowAllFiles()
            editorController.dispose()
        }
    }

    AisDmeWindow(onCloseRequest = ::exitApplication) {
        AisDmeTheme {
            Column {
                TabsBar(
                    tabs = editorController.tabs,
                    activeId = editorController.activeTabId,
                    onSelect = { editorController.activateTab(it) },
                    onClose = editorController::closeTab
                )

                editorController.activeTab?.let { tab ->
                    Editor(
                        text = tab.text,
                        error = tab.error,
                        onTextChange = editorController::updateText
                    )
                }
            }
        }
    }

    remember {
        CoroutineScope(Dispatchers.Swing).launch {
            editorController.openFile(File("test.asm"))
            editorController.openFile(File("test2.asm"))
            editorController.openFile(File("test3.asm"))
            println("opened file")
        }
    }

    println("the end of main")
}

