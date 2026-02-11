package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import non.era.aisdme.theme.aisDmeDialogProperties
import non.era.aisdme.theme.dialogBorder
import non.era.aisdme.ui.components.over.ButtonCancel
import non.era.aisdme.ui.components.over.ButtonOk

@Composable
fun FileNameDialog(
    title: String,
    initial: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    val error = remember(key1 = text) { validateFileName(text) }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        modifier = Modifier
            .dialogBorder()
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when (it.key) {
                    Key.Enter, Key.NumPadEnter -> {
                        if (error == null) {
                            onConfirm(text.trim())
                            onDismiss()
                        }
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

        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    isError = error != null,
                    singleLine = true,
                    label = { Text("Имя") },
                    modifier = Modifier.run {
                        fillMaxWidth().focusRequester(focusRequester)
                    }
                )

                if (error != null) {
                    Text(
                        error,
//                        color = MaterialTheme.colorScheme.error,
//                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },

        confirmButton = {
            ButtonOk(enabled = (error == null)) {
                onConfirm(text.trim())
                onDismiss()
            }
        },

        dismissButton = { ButtonCancel { onDismiss() } }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private val windowsReserved = setOf(
    "CON","PRN","AUX","NUL",
    "COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9",
    "LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9"
)

fun validateFileName(name: String): String? {
    if (name.isBlank()) return "Имя не может быть пустым"

    if (name.contains('/'))
        return "Имя не должно содержать /"

    val winInvalid = Regex("""[<>:"\\|?*]""")
    if (winInvalid.containsMatchIn(name))
        return "Недопустимые символы: <>:\"\\|?*"

    if (name.last() == ' ' || name.last() == '.')
        return "Имя не должно заканчиваться пробелом или точкой"

    if (windowsReserved.contains(name.uppercase()))
        return "Зарезервированное имя Windows"

    if (name.length > 255)
        return "Слишком длинное имя"

    return null
}