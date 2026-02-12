package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.focusable
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import non.era.aisdme.theme.aisDmeDialogProperties
import non.era.aisdme.theme.dialogBorder
import non.era.aisdme.ui.components.over.ButtonCancel
import non.era.aisdme.ui.components.over.ButtonOk

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        modifier = Modifier
            .dialogBorder()
            .focusRequester(focusRequester)
            .focusable()
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

