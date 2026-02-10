package non.era.aisdme.ui.components.dialogs

import androidx.compose.material.*
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(message)
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

