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
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

sealed interface UiDialog { //узлы берутся при запросе: диалог работает с тем, что было выделено
    data class ShowDeleteDialog(val nodes: List<ProjectNode>) : UiDialog
    data class ShowRenameDialog(val node: ProjectNode) : UiDialog
    data class ShowCreateObjDialog(val typeNew: TypeNew) : UiDialog
    data class ShowPasteDialog(val target: DirNode) : UiDialog
    data class ShowMoveDialog(val src: List<ProjectNode>, val target: File) : UiDialog
}

sealed interface GlobalMessageReq {
    data class ShowGlobalInfo(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalQuestion(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalSuccess(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalWarning(val title: String, val text: String): GlobalMessageReq
    data class ShowGlobalError(val text: String): GlobalMessageReq
}

data class TolltipState ( //неизменяемый: меняется только заменой целиком, иначе Compose не увидит
    val show: Boolean = false,
    val text: String = "",
    val position: Offset = Offset.Zero
)

class DialogsController(
    private val selectedNodes: () -> List<ProjectNode>,
) : UiController() {
    private val _effects = Channel<UiDialog>()
    val effects = _effects.receiveAsFlow()

    var tolltipState by mutableStateOf(TolltipState())
        private set

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

    fun requestDeleteDialog() {
        val nodes = selectedNodes()
        if(nodes.isNotEmpty()) requestDialog(UiDialog.ShowDeleteDialog(nodes)) //без выделения — молча
    }

    fun requestRenameDialog() {
        val node = selectedNodes().singleOrNull() ?: return error("Multi rename")
        requestDialog(UiDialog.ShowRenameDialog(node))
    }

    fun requestPasteDialog() {
        val selected = selectedNodes()
        val target = selected.singleOrNull() as? DirNode ?: return error(
            when {
                selected.isEmpty() -> "Directory for paste is not selected"
                selected.size != 1 -> "Pasting is only available in one directory."
                else -> "Pasting is only available in directory."
            }
        )
        requestDialog(UiDialog.ShowPasteDialog(target))
    }

    fun requestMoveDialog(src: List<ProjectNode>, target: File) {
        if(src.isEmpty()) return error("Selected file to move does not exist.")
        requestDialog(UiDialog.ShowMoveDialog(src, target))
    }

    private fun error(text: String) {
        scope.launch { newGlobalError(text) }
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