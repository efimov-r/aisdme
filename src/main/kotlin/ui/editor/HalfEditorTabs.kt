package non.era.aisdme.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Rect

class HalfEditorTabs(
    private var updateTabs: (tabs: List<Int>) -> Unit,
    private var updateActive: (activ: Int?) -> Unit
) {
    var dragDest by mutableStateOf(false)
    var inFocus by mutableStateOf(false)
    var rect: Rect? = null

    fun copy(updateTabs: (tabs: List<Int>) -> Unit,
             updateActive: (activ: Int?) -> Unit): HalfEditorTabs {
        val res = HalfEditorTabs(updateTabs, updateActive)
        this.updateTabs(listOf())
        this.updateTabs = {}
        this.updateActive(null)
        this.updateActive = {}
        res._listTabs = _listTabs.toList().toMutableStateList()
        res.updateTabs(res._listTabs)
        res._activeTabId = _activeTabId
        res.updateActive(res._activeTabId)
        return res
    }

    private var _listTabs = mutableStateListOf<Int>()
    val listTabs get(): SnapshotStateList<Int> = _listTabs

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
    }

    fun closeTab(id: Int) {
        _listTabs.remove(id)
        updateTabs(_listTabs)
        if (_activeTabId == id) {
            _activeTabId = _listTabs.lastOrNull()
            updateActive(_activeTabId)
        }
    }

    fun closeOtherTabs(id: Int) {
        _listTabs.removeIf { it != id }
        updateTabs(_listTabs)
        if (_activeTabId != id) {
            _activeTabId = id
            updateActive(_activeTabId)
        }
    }

    fun closeLeftTabs(id: Int) {
        val index = _listTabs.indexOf(id)
        if (index != -1) {
            _listTabs.subList(0, index).clear()
        }
        updateTabs(_listTabs)
        if (!_listTabs.contains(_activeTabId)) {
            _activeTabId = id
            updateActive(_activeTabId)
        }
    }

    fun closeRightTabs(id: Int) {
        val index = _listTabs.indexOf(id)
        if (index != -1 && index < _listTabs.size - 1) {
            _listTabs.subList(index + 1, _listTabs.size).clear()
        }
        updateTabs(_listTabs)
        if (!_listTabs.contains(_activeTabId)) {
            _activeTabId = id
            updateActive(_activeTabId)
        }
    }

    fun closeAllTabs() {
        _activeTabId = null
        updateActive(_activeTabId)
        _listTabs.removeAll{ true }
        updateTabs(_listTabs)
    }
}