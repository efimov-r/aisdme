package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class EditorController(
    private val autosaveDelayMs: Long = 2000
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextTabId = 0
    private val nextTabIdMutex = Mutex()
    private val updateTabMutex = Mutex()

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

        val newId = nextTabIdMutex.withLock {
            nextTabId++
        }

        val tab = runCatching {
            val text = if (file.exists()) file.readText() else ""
            ObservableEditorTab(
                id = newId,
                file = file,
                textIn = text,
                savedTextIn = text
            )
        }.getOrElse {
            ObservableEditorTab(
                id = newId,
                file = file,
                textIn = "",
                savedTextIn = "",
                errorIn = it.message
            )
        }

        tabs = tabs + tab
        activeTabId = tab.id
    }

    fun updateText(newText: String) {
        activeTab?.setTextWithUpTitle(newText, scope)
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

    fun saveNowAllFiles() {
        tabs.forEach { tab->
            scope.launch {
                tab.autosaveJob?.cancel()
                saveTabFile(tab)
            }
        }
    }

    private suspend fun saveTabFile(tab: ObservableEditorTab) {
        val file = tab.file ?: return
        if (!file.canWrite()) return

        runCatching {
            tab.saveMutex.withLock {
                file.writeText(tab.text)
            }
            withContext(Dispatchers.Swing){
                tab.error = null
                tab.setSavedTextWithUpTitle(tab.text, scope)
            }

        }.getOrElse {
            withContext(Dispatchers.Swing){
                tab.error = it.message
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }
}