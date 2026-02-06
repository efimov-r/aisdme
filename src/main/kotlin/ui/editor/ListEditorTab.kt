package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.io.File

class ListEditorTab(
    private val autosaveDelayMs: Long = 2000
) {
    private var tabs_ by mutableStateOf<List<ObservableEditorTab>>(emptyList())
    private var activeTabId_ by mutableStateOf<Int?>(null)

    fun getTabs() : List<ObservableEditorTab> {
        return tabs_
    }

    fun getActiveTabId(): Int? {
        return activeTabId_
    }

    fun getActiveTab(): ObservableEditorTab? {
        return tabs_.find { it.id == activeTabId_ }
    }





    fun activateTab(tabId: Int) {
        activeTabId_ = tabId
    }

    suspend fun openFile(file: File) = withContext(Dispatchers.Swing) {
        // если файл уже открыт — просто активируем
        tabs_.find { it.file == file }?.let {
            activeTabId_ = it.id
            return@withContext
        }

        val tab = runCatching {
            val text = if (file.exists()) file.readText() else ""
            ObservableEditorTab.createNew(file, text, null)
        }.getOrElse {
            ObservableEditorTab.createNew(file, "", it.message)
        }

        tabs_ = tabs_ + tab
        activeTabId_ = tab.id
    }

    fun updateActiveTabText(newText: String) {
        getActiveTab()?.setText(newText)
    }

    suspend fun updateActiveTabTitle() {
        getActiveTab()?.updateTitle()
    }

    fun closeTab(id: Int) {
        tabs_ = tabs_.filterNot { it.id == id }

        if (activeTabId_ == id) {
            activeTabId_ = tabs_.lastOrNull()?.id
        }
    }

    fun scheduleAutosaveActive(scope: CoroutineScope) =
        getActiveTab()?.let{ tab->
            tab.autosaveJob?.cancel()

            tab.autosaveJob = scope.launch {
                delay(autosaveDelayMs)
                if (!tab.isModified) return@launch
                saveTabFile(tab, scope)
            }
        }

    //todo для saveAll упрощенный?
    private suspend fun saveTabFile(tab: ObservableEditorTab, scope: CoroutineScope) {
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

    fun saveAll(scope: CoroutineScope) =
        tabs_.map { ta->
            scope.launch {
                ta.autosaveJob?.cancel()
                saveTabFile(ta, scope)
            }
        }
}