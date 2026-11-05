package non.era.aisdme.configs.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import non.era.aisdme.ui.editor.halfviewmodel.TabCash

class EditorStateRepository { //запись присваиванием value: потокобезопасно и в порядке вызовов; списки — копией
    private val _files = MutableStateFlow<Map<Int, TabCash>?>(null)
    val files = _files.asStateFlow()
    fun updateFiles(files: Map<Int, TabCash>) { _files.value = files }

    private val _leftTabs = MutableStateFlow<List<Int>?>(null)
    val leftTabs get() = _leftTabs.asStateFlow()
    fun updateLeftTabs(leftTabs: List<Int>) { _leftTabs.value = leftTabs.toList() }

    private val _rightTabs = MutableStateFlow<List<Int>?>(null)
    val rightTabs get() = _rightTabs.asStateFlow()
    fun updateRightTabs(rightTabs: List<Int>) { _rightTabs.value = rightTabs.toList() }

    private val _leftActive = MutableStateFlow<Int?>(null)
    val leftActive get() = _leftActive.asStateFlow()
    fun updateLeftActiveTabs(leftActive: Int?) { _leftActive.value = leftActive }

    private val _rightActive = MutableStateFlow<Int?>(null)
    val rightActive get() = _rightActive.asStateFlow()
    fun updateRightActiveTabs(rightActive: Int?) { _rightActive.value = rightActive }

    fun reset() { //новый проект: состояние старого не должно попасть в его конфиг
        _files.value = null
        _leftTabs.value = null
        _rightTabs.value = null
        _leftActive.value = null
        _rightActive.value = null
    }
}