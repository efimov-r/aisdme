package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

class HalfEditorTabs(
    private var files: MutableMap<Int, ObservableOpenedFile>,
    private val autosaveDelayMs: Long,
    private val scope: CoroutineScope
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
        return activeTabId_ ?: listTabs.lastOrNull()
    }

    override fun getActiveTab(): ObservableOpenedFile? {
        return files[activeTabId_]
    }

    override fun activateTab(tabId: Int) {
        activeTabId_ = tabId
    }

    override fun countTabs(): Int = listTabs.size

    fun openFile(file: File) {
        files.values.find { it.file == file }?.id?.let { tabMapId ->
            if (!listTabs.contains(tabMapId)) listTabs.add(tabMapId)
            activeTabId_ = tabMapId
        } ?: run {
            val tabEmpty = ObservableOpenedFile.createEmpty(file)
            files[tabEmpty.id] = tabEmpty
            listTabs.add(tabEmpty.id)
            activeTabId_ = tabEmpty.id
            scope.async {
                runCatching {
                    delay(300) //todo debug!!
                    val text = if (file.exists()) file.readText() else ""
                    withContext(Dispatchers.Swing) {
                        listMutex.withLock {
                            tabEmpty.initText(text)
                            tabEmpty.readOnly = false
                        }
                    }
                }.getOrElse {
                    withContext(Dispatchers.Swing) {
                        listMutex.withLock {
                            tabEmpty.error = it.message
                        }
                    }
                }
            }
        }
    }

    override fun updateText(newText: String) {
        val activTabLocal = files[activeTabId_]
        activTabLocal?.setText(newText)
        scope.launch {
            activTabLocal?.updateTitle()
        }
        activTabLocal?.let{ tab->
            tab.autosaveJob?.cancel()

            tab.autosaveJob = scope.launch {
                delay(autosaveDelayMs)
                if (!tab.isModified) return@launch
                if (tab.fileNotCanWrite()) return@launch

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
    }

    override fun closeTab(id: Int) {
        listTabs.remove(id)

        if (activeTabId_ == id) {
            activeTabId_ = listTabs.lastOrNull()
        }
    }

    fun closeAllTabs() {
        listTabs.forEach { id->
            listTabs.remove(id)

            if (activeTabId_ == id) {
                activeTabId_ = listTabs.lastOrNull()
            }
        }
    }

    fun saveAll() {
        runBlocking {
            files.values.map { ta ->
                scope.launch {
                    ta.autosaveJob?.cancel()
                    if (ta.fileNotCanWrite()) return@launch
                    runCatching {
                        ta.writeTextToFile()
                    }
                    listMutex.withLock {
                        files.remove(ta.id)
                        listTabs.remove(ta.id)
                    }
                }
            }.joinAll()
        }
    }
}