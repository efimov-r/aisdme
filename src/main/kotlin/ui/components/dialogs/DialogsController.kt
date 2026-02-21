package non.era.aisdme.ui.components.dialogs

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import non.era.aisdme.ui.UiController

sealed interface UiDialog {
    object ShowDeleteDialog : UiDialog
    data class ShowCreateObjDialog(val typeNew: TypeNew) : UiDialog
}

class DialogsController : UiController() {
    private val _effects = Channel<UiDialog>()
    val effects = _effects.receiveAsFlow()

    fun requestDialog(dialog: UiDialog) {
        scope.launch {
            _effects.send(dialog)
        }
    }
}