package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.io.File

class EditorController(
    private val autosaveDelayMs: Long = 2000
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var tabs by mutableStateOf<List<ObservableEditorTab>>(emptyList())
        private set

    var activeTabId by mutableStateOf<Int?>(null)
        private set

    val activeTab: ObservableEditorTab?
        get() = tabs.find { it.id == activeTabId }

    fun activateTab(tabId: Int) {
        activeTabId = tabId
    }

    suspend fun openFile(file: File) = withContext(Dispatchers.Swing) {
        // если файл уже открыт — просто активируем
        tabs.find { it.file == file }?.let {
            activeTabId = it.id
            return@withContext
        }

        val tab = runCatching {
            val text = if (file.exists()) file.readText() else ""
            ObservableEditorTab.createNew(file, text, null)
        }.getOrElse {
            ObservableEditorTab.createNew(file, "", it.message)
        }

        tabs = tabs + tab
        activeTabId = tab.id
    }

    fun updateText(newText: String) {
        activeTab?.setText(newText, scope)
        scheduleAutosaveActive()
    }

    fun saveActive() {
        val tab = activeTab ?: return
        scope.launch {
            saveTabFile(tab)
        }
    }

    fun closeTab(id: Int) {
        tabs = tabs.filterNot { it.id == id }

        if (activeTabId == id) {
            activeTabId = tabs.lastOrNull()?.id
        }
    }

    private fun scheduleAutosaveActive() =
        activeTab?.let{ tab->
            tab.autosaveJob?.cancel()

            tab.autosaveJob = scope.launch {
                delay(autosaveDelayMs)
                if (!tab.isModified) return@launch
                saveTabFile(tab)
            }
        }

    suspend fun saveAllAndDispose() {
        tabs.map { tab->
            scope.launch {
                tab.autosaveJob?.cancel()
                saveTabFile(tab)
            }
        }.joinAll()
        scope.cancel()
    }

    private suspend fun saveTabFile(tab: ObservableEditorTab) {
        if (tab.fileNotCanWrite()) return

        runCatching {
            tab.writeTextToFile()
            withContext(Dispatchers.Swing){
                tab.error = null
                tab.setSavedText(tab.getText(), scope)
            }

        }.getOrElse {
            withContext(Dispatchers.Swing){
                tab.error = it.message
            }
        }
    }
}