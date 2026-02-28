package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList

class HalfEditorTabs(
    private val updateTabs: (tabs: List<Int>) -> Unit,
    private val updateActive: (activ: Int?) -> Unit
) {
    var inFocus by mutableStateOf(false)

    fun copy(updateTabs: (tabs: List<Int>) -> Unit,
             updateActive: (activ: Int?) -> Unit): HalfEditorTabs {
        val res = HalfEditorTabs(updateTabs, updateActive)
        res._listTabs = _listTabs.toList().toMutableStateList()
        res._activeTabId = _activeTabId
        return res
    }

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
        if (!_listTabs.contains(tabId)) {
            _listTabs.add(tabId)
            updateTabs(_listTabs)
        }
        _activeTabId = tabId
        updateActive(_activeTabId)
        inFocus = true
    }

    fun countTabs(): Int = _listTabs.size

    fun closeTab(id: Int) {
        _listTabs.remove(id)
        updateTabs(_listTabs)
        if (_activeTabId == id) {
            _activeTabId = _listTabs.lastOrNull()
            updateActive(_activeTabId)
        }
    }

    fun closeAllTabs() {
        _activeTabId = null
        _listTabs.removeAll{ true }
    }
}