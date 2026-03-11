package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope

abstract class ObservableOpenedHalf(
    val id: Int,
    protected val _extScope: CoroutineScope,
) {
    enum class OpenFileType {
        OPEN_NEW_FILE, OPEN_FROM_CASH
    }

    protected abstract var _name: String
    val name get() = _name

    abstract fun copy() : ObservableOpenedHalf

    abstract suspend fun beforeRemove()

    abstract suspend fun disposeBeforeBlocked()

    abstract suspend fun openFile(typeOpen: OpenFileType)
}