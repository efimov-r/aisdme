package non.era.aisdme.ui.components.dialogs.elements

import androidx.compose.runtime.Composable
import non.era.aisdme.LocalAppController
import non.era.aisdme.ui.components.dialogs.DeleteSelectedNodeDialog
import non.era.aisdme.ui.components.dialogs.NewNodeDialog
import non.era.aisdme.ui.components.dialogs.UiDialog

@Composable
fun DialogsStarter(activeDialog: UiDialog?, onDismiss: () -> Unit) {
    activeDialog?.let { dialog ->
        when (dialog) {
            is UiDialog.ShowDeleteDialog -> {
                DeleteSelectedNodeDialog(onDismiss)
            }
            is UiDialog.ShowCreateObjDialog -> {
                NewNodeDialog(dialog.typeNew, onDismiss)
            }
        }
    }
}