package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.AppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.theme.aisDmeDialogProperties
import non.era.aisdme.theme.dialogBorder
import non.era.aisdme.ui.components.over.ButtonCancel
import non.era.aisdme.ui.components.over.ButtonOk
import non.era.aisdme.ui.project.DirNode

enum class TypeNew {
    FILE, DIRECTORY
}

@Composable
fun NewNodeDialog(
    app: AppController,
    insertNode: DirNode,
    typeNew: TypeNew,
    onDismiss: () -> Unit
) = when(typeNew) {
        TypeNew.FILE -> {
            FileNodeDialog(
                title = "Create new File",
                initial = "filename",
                onConfirm = {
                    app.createFile(insertNode.file.toPath().resolve(it))
                },
                onDismiss = onDismiss
            )
        }
        TypeNew.DIRECTORY -> {
            FileNodeDialog(
                title = "Create new Directory",
                initial = "name directory",
                onConfirm = {
                    app.createDirectory(insertNode.file.toPath().resolve(it))
                },
                onDismiss = onDismiss
            )
        }
    }

@Composable
private fun FileNodeDialog(
    title: String,
    initial: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
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
            Column(modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    isError = error != null,
                    singleLine = true,
                    placeholder = { Text(initial) }, // Вместо label используем placeholder
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = LocalAisDmeColors.current.primary, // Цвет черты при фокусе
                        unfocusedIndicatorColor = LocalAisDmeColors.current.surfaceBorder, // Цвет черты без фокуса
                        disabledIndicatorColor = LocalAisDmeColors.current.surfaceBorder, // Цвет черты для disabled
                        errorIndicatorColor = LocalAisDmeColors.current.error, // Цвет черты при ошибке
                        cursorColor = LocalAisDmeColors.current.primary, // Цвет курсора
                        backgroundColor = LocalAisDmeColors.current.transparent // Прозрачный фон
                    )
                )

                if (error != null) {
                    Text(
                        error,
                        color = LocalAisDmeColors.current.error,
                        style = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 20.sp)
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
    if (name.isBlank()) return "The name cannot be empty"

    if (name.contains('/'))
        return "The name must not contain /"

    val winInvalid = Regex("""[<>:"\\|?*]""")
    if (winInvalid.containsMatchIn(name))
        return "Invalid characters: <>:\"\\|?*"

    if (name.last() == ' ' || name.last() == '.')
        return "The name must not end with a space or dot."

    if (windowsReserved.contains(name.uppercase()))
        return "Windows reserved"

    if (name.length > 255)
        return "The name is too long"

    return null
}