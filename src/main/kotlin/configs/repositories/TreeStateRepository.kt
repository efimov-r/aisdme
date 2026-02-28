package non.era.aisdme.configs.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import non.era.aisdme.configs.dataclass.child.TreeStateConfig

class TreeStateRepository {
    private val _tree = MutableStateFlow<TreeStateConfig?>(null)
    val tree = _tree.asStateFlow()
    suspend fun updateTree(tree: TreeStateConfig) { _tree.emit(tree) }
}