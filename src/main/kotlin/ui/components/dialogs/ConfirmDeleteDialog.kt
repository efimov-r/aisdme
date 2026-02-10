package non.era.aisdme.ui.components.dialogs

import androidx.compose.material.*
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDeleteDialog(
    fileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete file?")
        },
        text = {
            Text("File \"$fileName\" will be deleted without the possibility of recovery.")
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                )
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss,
            ) {
                Text("Cancel")
            }
        }
    )
}

