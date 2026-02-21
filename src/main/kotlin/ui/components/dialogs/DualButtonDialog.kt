package non.era.aisdme.ui.components.dialogs

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import non.era.aisdme.theme.aisDmeDialogProperties
import non.era.aisdme.theme.dialogBorder
import non.era.aisdme.ui.components.over.ButtonCancel
import non.era.aisdme.ui.components.over.ButtonOk

@Composable
fun DualButtonDialog(
    modifier: Modifier = Modifier,
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)?
) = AlertDialog(
        modifier = modifier
            .dialogBorder()
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when (it.key) {
                    Key.Enter, Key.NumPadEnter -> {
                        onConfirm()
                        onDismiss()
                        true
                    }
                    Key.Escape -> {
                        onDismiss()
                        true
                    }
                    else -> false
                }
            },
        onDismissRequest = onDismiss,
        properties = aisDmeDialogProperties(),
        title = { Text(title) },
        text = content,
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