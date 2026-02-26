package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.BuildConfig
import java.io.File

class HalfEditorTabs(
    private var files: MutableMap<Int, ObservableOpenedFile>,
    private val eScope: CoroutineScope
) : EditorHalfState {
    private val listMutex: Mutex = Mutex()

    private var listTabs = mutableStateListOf<Int>()
    private var activeTabId_ by mutableStateOf<Int?>(null)

    override fun getTabs() : List<ObservableOpenedFile> {
        return listTabs.mapNotNull { tId->
            files[tId]
        }
    }

    override fun getActiveTabId(): Int? {
        return activeTabId_ ?: run {
            activeTabId_ = listTabs.lastOrNull()
            activeTabId_
        }
    }

    override fun getActiveTab(): ObservableOpenedFile? {
        return files[activeTabId_]
    }

    override fun activateTab(tabId: Int) {
        if (!listTabs.contains(tabId)) listTabs.add(tabId)
        activeTabId_ = tabId
    }

    override fun countTabs(): Int = listTabs.size

    override fun updateText(newText: String) {
        val activTabLocal = files[activeTabId_]
        activTabLocal?.let{it.text = newText}
        eScope.launch {
            activTabLocal?.updateTitle()
        }
        activTabLocal?.let{ tab->
            tab.autosaveJob?.cancel()

            tab.autosaveJob = eScope.launch {
                delay(2000)
                if (!tab.isModified) return@launch
                if (!tab.file.canWrite()) return@launch

                runCatching {
                    tab.writeTextToFile()
                    withContext(Dispatchers.Swing){
                        tab.error = null
                        tab.setSavedText(tab.text, eScope)
                    }

                }.getOrElse {
                    withContext(Dispatchers.Swing){
                        tab.error = it.message
                    }
                }
            }
        }
    }

    override fun closeTab(id: Int) {
        listTabs.remove(id)

        if (activeTabId_ == id) {
            activeTabId_ = listTabs.lastOrNull()
        }
    }

    fun closeAllTabs() {
        activeTabId_ = null
        listTabs.removeAll{ true }
    }
}