package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class HalfEditorTabs {
    private var _listTabs = mutableStateListOf<Int>()
    val listTabs get() = _listTabs

    private var _activeTabId by mutableStateOf<Int?>(null)
    val activeTabId get() = _activeTabId

    //загрузка из конфига
    fun reselectProject(tabs: List<Int>?, tabSelect: Int?) {
        _listTabs.clear()
        tabs?.let { _listTabs.addAll(it) }
        _activeTabId = tabSelect
        if(_activeTabId == null) _activeTabId = _listTabs.lastOrNull()
    }

    fun activateTab(tabId: Int) {
        if (!_listTabs.contains(tabId)) _listTabs.add(tabId)
        _activeTabId = tabId
    }

    fun countTabs(): Int = _listTabs.size

    fun closeTab(id: Int) {
        _listTabs.remove(id)
        if (_activeTabId == id) _activeTabId = _listTabs.lastOrNull()
    }

    fun closeAllTabs() {
        _activeTabId = null
        _listTabs.removeAll{ true }
    }
}