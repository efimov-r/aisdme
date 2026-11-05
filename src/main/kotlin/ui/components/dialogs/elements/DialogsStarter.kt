package non.era.aisdme.ui.components.dialogs.elements

import androidx.compose.runtime.Composable
import non.era.aisdme.ui.components.dialogs.*

@Composable
fun DialogsStarter(activeDialog: UiDialog?, onDismiss: () -> Unit) {
    activeDialog?.let { dialog ->
        when (dialog) {
            is UiDialog.ShowRenameDialog -> RenameNodeDialog(dialog.node, onDismiss)
            is UiDialog.ShowDeleteDialog -> DeleteSelectedNodeDialog(dialog.nodes, onDismiss)
            is UiDialog.ShowCreateObjDialog -> NewNodeDialog(dialog.typeNew, dialog.dir, onDismiss)
            is UiDialog.ShowPasteDialog -> PasteDialog(dialog.target, onDismiss)
            is UiDialog.ShowMoveDialog -> MoveDialog(dialog.src, dialog.target, onDismiss)
        }
    }
}