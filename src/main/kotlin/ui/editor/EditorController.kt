package non.era.aisdme.ui.editor

import kotlinx.coroutines.*
import java.io.File

class EditorController(
    autosaveDelayMs: Long = 2000
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val leftTabs = ListEditorTab(autosaveDelayMs)

    fun activateTab(tabId: Int) =
        leftTabs.activateTab(tabId)

    suspend fun openFile(file: File) =
        leftTabs.openFile(file)

    fun updateText(newText: String) {
        leftTabs.updateActiveTabText(newText)
        scope.launch {
            leftTabs.updateActiveTabTitle()
        }
        leftTabs.scheduleAutosaveActive(scope)
    }

    fun closeTab(id: Int) =
        leftTabs.closeTab(id)


    suspend fun saveAllAndDispose() {
        leftTabs.saveAll(scope).joinAll()
        scope.cancel()
    }
}