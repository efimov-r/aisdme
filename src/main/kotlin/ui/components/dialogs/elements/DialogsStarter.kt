package non.era.aisdme.ui.components.dialogs.elements

import androidx.compose.runtime.Composable
import non.era.aisdme.ui.components.dialogs.*

@Composable
fun DialogsStarter(activeDialog: UiDialog?, onDismiss: () -> Unit) {
    activeDialog?.let { dialog ->
        when (dialog) {
            is UiDialog.ShowDeleteDialog -> DeleteSelectedNodeDialog(onDismiss)
            is UiDialog.ShowCreateObjDialog -> NewNodeDialog(dialog.typeNew, onDismiss)
            is UiDialog.ShowPasteDialog -> PasteDialog(onDismiss)
            is UiDialog.ShowMoveDialog -> MoveDialog(dialog.src, dialog.target, onDismiss)
        }
    }
}