package non.era.aisdme.configs.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import non.era.aisdme.configs.dataclass.child.SnapshotTree

class TreeStateRepository {
    private val _tree = MutableStateFlow<SnapshotTree?>(null)
    val tree = _tree.asStateFlow()
    suspend fun updateTree(tree: SnapshotTree) { _tree.emit(tree) }

    fun reset() { //новый проект: дерево старого не должно попасть в его конфиг
        _tree.value = null
    }
}