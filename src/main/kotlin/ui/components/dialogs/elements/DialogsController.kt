package non.era.aisdme.ui.components.dialogs.elements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.components.dialogs.TypeNew
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

sealed interface UiDialog {
    object ShowDeleteDialog : UiDialog
    data class ShowCreateObjDialog(val typeNew: TypeNew) : UiDialog
    object ShowPasteDialog : UiDialog
    data class ShowMoveDialog(val src: List<ProjectNode>, val target: File) : UiDialog
}

sealed interface GlobalMessageReq {
    data class ShowGlobalInfo(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalQuestion(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalSuccess(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalWarning(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalError(val text: String): GlobalMessageReq
}

data class TolltipState (
    var show: Boolean = false,
    var text: String = "",
    var position: Offset = Offset.Zero
)

class DialogsController : UiController() {
    private val _effects = Channel<UiDialog>()
    val effects = _effects.receiveAsFlow()

    var tolltipState by mutableStateOf(TolltipState())

    fun tolltipShow(text: String, position: Offset) {
        tolltipState = tolltipState.copy(show = true, text = text, position = position)
    }

    fun tolltipClose() {
        tolltipState = TolltipState(show = false)
    }


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