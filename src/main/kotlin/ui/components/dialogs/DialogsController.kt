package non.era.aisdme.ui.components.dialogs

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import non.era.aisdme.ui.UiController

sealed interface UiDialog {
    object ShowDeleteDialog : UiDialog
    data class ShowCreateObjDialog(val typeNew: TypeNew) : UiDialog
    object ShowPasteDialog : UiDialog
}

sealed interface GlobalMessageReq {
    data class ShowGlobalInfo(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalQuestion(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalSuccess(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalWarning(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalError(val text: String): GlobalMessageReq
}

class DialogsController : UiController() {
    private val _effects = Channel<UiDialog>()
    val effects = _effects.receiveAsFlow()

    fun requestDialog(dialog: UiDialog) {
        scope.launch {
            _effects.send(dialog)
        }
    }
    
    fun requestGlobalMessage(mess: GlobalMessageReq) {
        scope.launch {
            when (mess) {
                is GlobalMessageReq.ShowGlobalInfo -> newGlobalInfo(mess.title, mess.text)
                is GlobalMessageReq.ShowGlobalQuestion -> newGlobalQuestion(mess.title, mess.text)
                is GlobalMessageReq.ShowGlobalSuccess -> newGlobalSuccess(mess.title, mess.text)
                is GlobalMessageReq.ShowGlobalWarning -> newGlobalWarning(mess.title, mess.text)
                is GlobalMessageReq.ShowGlobalError -> newGlobalError(mess.text)
            }
        }
    }
}