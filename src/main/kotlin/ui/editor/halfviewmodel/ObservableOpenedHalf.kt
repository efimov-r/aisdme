package non.era.aisdme.ui.editor.halfviewmodel

import kotlinx.coroutines.CoroutineScope

abstract class ObservableOpenedHalf(
    val id: Int,
    protected val _extScope: CoroutineScope,
) {
    protected abstract var _name: String
    val name get() = _name

    abstract fun copy() : ObservableOpenedHalf

    abstract suspend fun beforeRemove()

    abstract suspend fun disposeBeforeBlocked()
}