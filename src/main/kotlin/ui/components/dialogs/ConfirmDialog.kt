package non.era.aisdme.ui.components.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import non.era.aisdme.theme.aisDmeDialogProperties
import non.era.aisdme.theme.dialogBorder
import non.era.aisdme.ui.components.over.ButtonCancel
import non.era.aisdme.ui.components.over.ButtonOk

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.dialogBorder(),
        onDismissRequest = onDismiss,
        properties = aisDmeDialogProperties(),
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            ButtonOk {
                onConfirm()
                onDismiss()
            }
        },
        dismissButton = {
            ButtonCancel {
                onDismiss()
            }
        }
    )
}

