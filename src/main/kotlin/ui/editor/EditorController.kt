package non.era.aisdme.ui.editor

import kotlinx.coroutines.*
import java.io.File

class EditorController(
    autosaveDelayMs: Long = 2000
) {
    val leftTabs = ListEditorTab(autosaveDelayMs)

    suspend fun openFile(file: File) =
        leftTabs.openFile(file)

    suspend fun saveAllAndDispose() {
        leftTabs.saveAll().joinAll()
        leftTabs.dispose()
    }
}