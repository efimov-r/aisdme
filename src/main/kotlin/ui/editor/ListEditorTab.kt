package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.io.File

class ListEditorTab(
    private val autosaveDelayMs: Long
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var tabs2: MutableMap<Int, ObservableEditorTab> = mutableMapOf()
    private var listTabs = mutableStateListOf<Int>()
    private var activeTabId_ by mutableStateOf<Int?>(null)

    fun getTabs() : List<ObservableEditorTab> {
        return listTabs.mapNotNull { tId->
            tabs2[tId]
        }
    }

    fun getActiveTabId(): Int? {
        return activeTabId_
    }

    fun getActiveTab(): ObservableEditorTab? {
        return tabs2[activeTabId_]
    }

    fun activateTab(tabId: Int) {
        activeTabId_ = tabId
    }

    suspend fun openFile(file: File) = withContext(Dispatchers.Swing) {
        tabs2.values.find { it.file == file }?.id?.let { tabMapId->
            if(!listTabs.contains(tabMapId)) listTabs.add(tabMapId)
            activeTabId_ = tabMapId
        } ?: run {
            val tab = runCatching {
                val text = if (file.exists()) file.readText() else ""
                ObservableEditorTab.createNew(file, text, null)
            }.getOrElse {
                ObservableEditorTab.createNew(file, "", it.message)
            }
            tabs2.set(tab.id, tab)
            if(!listTabs.contains(tab.id)) listTabs.add(tab.id)
            activeTabId_ = tab.id
        }
    }

    fun updateText(newText: String) {
        updateActiveTabText(newText)
        scope.launch {
            updateActiveTabTitle()
        }
        scheduleAutosaveActive()
    }

    fun updateActiveTabText(newText: String) {
        getActiveTab()?.setText(newText)
    }

    suspend fun updateActiveTabTitle() {
        getActiveTab()?.updateTitle()
    }

    fun closeTab(id: Int) {
        tabs2.remove(id) //todo для перечитывания файла при открытии вкладки заново
        listTabs.remove(id)

        if (activeTabId_ == id) {
            activeTabId_ = listTabs.lastOrNull()
        }
    }

    fun scheduleAutosaveActive() =
        getActiveTab()?.let{ tab->
            tab.autosaveJob?.cancel()

            tab.autosaveJob = scope.launch {
                delay(autosaveDelayMs)
                if (!tab.isModified) return@launch
                saveTabFile(tab)
            }
        }

    //todo для saveAll упрощенный?
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

    fun saveAll() =
        tabs2.values.map { ta->
            scope.launch {
                ta.autosaveJob?.cancel()
                saveTabFile(ta)
            }
        }

    fun dispose() {
        scope.cancel()
    }
}