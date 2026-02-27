package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

class HalfEditorTabs(
    private var files: Map<Int, ObservableOpenedFile>,
    private val eScope: CoroutineScope //todo хуйня
) : EditorHalfState {
    private var _listTabs = mutableStateListOf<Int>()
    val listTabs: List<Int> get() = _listTabs.toList()

    private var _activeTabId by mutableStateOf<Int?>(null)

    fun reselectProject(tabs: List<Int>?, tabSelect: Int?) {
        _listTabs.clear()
        tabs?.let { _listTabs.addAll(it) }
        _activeTabId = tabSelect
    }

    //todo не обновляется налету
    override fun getTabs() : List<ObservableOpenedFile> {
        return _listTabs.mapNotNull { tId->
            files[tId]
        }
    }

    override fun getActiveTabId(): Int? {
        return _activeTabId ?: run {
            _activeTabId = _listTabs.lastOrNull()
            _activeTabId
        }
    }

    override fun getActiveTab(): ObservableOpenedFile? {
        return files[_activeTabId]
    }

    override fun activateTab(tabId: Int) {
        if (!_listTabs.contains(tabId)) _listTabs.add(tabId)
        _activeTabId = tabId
    }

    override fun countTabs(): Int = _listTabs.size

    override fun updateText(newText: String) {
        files[_activeTabId]?.updateTextAndAutosave(newText, eScope)
    }

    override fun closeTab(id: Int) {
        _listTabs.remove(id)
        if (_activeTabId == id) _activeTabId = _listTabs.lastOrNull()
    }

    fun closeAllTabs() {
        _activeTabId = null
        _listTabs.removeAll{ true }
    }
}