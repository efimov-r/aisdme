package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope
import non.era.aisdme.configs.dataclass.snapshots.SnapshotTabCash

abstract class TabCash(
    val id: Int,
    protected val _extScope: CoroutineScope,
) {
    enum class OpenFileType {
        OPEN_NEW_FILE, OPEN_FROM_CASH
    }

    protected abstract var _name: String
    val name get() = _name

    abstract fun copy() : TabCash

    abstract suspend fun beforeRemove()

    abstract suspend fun disposeBeforeBlocked()

    abstract suspend fun openFile(typeOpen: OpenFileType)

    abstract fun toSnapshot() : SnapshotTabCash?
}