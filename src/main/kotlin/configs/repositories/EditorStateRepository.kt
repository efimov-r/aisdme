package non.era.aisdme.configs.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import non.era.aisdme.ui.editor.halfviewmodel.TabCash

class EditorStateRepository {
    private val _files = MutableStateFlow<Map<Int, TabCash>?>(null)
    val files = _files.asStateFlow()
    suspend fun updateFiles(files: Map<Int, TabCash>) { _files.emit(files) }

    private val _leftTabs = MutableStateFlow<List<Int>?>(null)
    val leftTabs get() = _leftTabs.asStateFlow()
    suspend fun updateLeftTabs(leftTabs: List<Int>) { _leftTabs.emit(leftTabs) }

    private val _rightTabs = MutableStateFlow<List<Int>?>(null)
    val rightTabs get() = _rightTabs.asStateFlow()
    suspend fun updateRightTabs(rightTabs: List<Int>) { _rightTabs.emit(rightTabs) }

    private val _leftActive = MutableStateFlow<Int?>(null)
    val leftActive get() = _leftActive.asStateFlow()
    suspend fun updateLeftActiveTabs(leftActive: Int?) { _leftActive.emit(leftActive) }

    private val _rightActive = MutableStateFlow<Int?>(null)
    val rightActive get() = _rightActive.asStateFlow()
    suspend fun updateRightActiveTabs(rightActive: Int?) { _rightActive.emit(rightActive) }

    fun reset() { //новый проект: состояние старого не должно попасть в его конфиг
        _files.value = null
        _leftTabs.value = null
        _rightTabs.value = null
        _leftActive.value = null
        _rightActive.value = null
    }
}