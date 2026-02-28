package non.era.aisdme.configs.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import non.era.aisdme.ui.editor.ObservableOpenedFile

class EditorStateRepository {
    private val _files = MutableStateFlow<Map<Int, ObservableOpenedFile>?>(null)
    val files = _files.asStateFlow()
    suspend fun updateFiles(files: Map<Int, ObservableOpenedFile>) { _files.emit(files) }

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
}