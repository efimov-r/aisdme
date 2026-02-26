package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

class HalfEditorTabs(
    private var files: MutableMap<Int, ObservableOpenedFile>,
    private val eScope: CoroutineScope //todo хуйня
) : EditorHalfState {
    private var listTabs = mutableStateListOf<Int>()
    private var _activeTabId by mutableStateOf<Int?>(null)

    override fun getTabs() : List<ObservableOpenedFile> {
        return listTabs.mapNotNull { tId->
            files[tId]
        }
    }

    override fun getActiveTabId(): Int? {
        return _activeTabId ?: run {
            _activeTabId = listTabs.lastOrNull()
            _activeTabId
        }
    }

    override fun getActiveTab(): ObservableOpenedFile? {
        return files[_activeTabId]
    }

    override fun activateTab(tabId: Int) {
        if (!listTabs.contains(tabId)) listTabs.add(tabId)
        _activeTabId = tabId
    }

    override fun countTabs(): Int = listTabs.size

    override fun updateText(newText: String) {
        files[_activeTabId]?.updateTextAndAutosave(newText, eScope)
    }

    override fun closeTab(id: Int) {
        listTabs.remove(id)
        if (_activeTabId == id) _activeTabId = listTabs.lastOrNull()
    }

    fun closeAllTabs() {
        _activeTabId = null
        listTabs.removeAll{ true }
    }
}