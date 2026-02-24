package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.ui.components.dialogs.elements.DualButtonDialog
import non.era.aisdme.ui.components.over.ErrorableTextField

enum class TypeNew {
    FILE, DIRECTORY
}

@Composable
fun NewNodeDialog(
    typeNew: TypeNew,
    onDismiss: () -> Unit
) {
    val app = LocalAppController.current
    when (typeNew) {
        TypeNew.FILE -> {
            FileNodeDialog(
                title = "Create new File",
                placeholder = "filename",
                onConfirm = { app.createFileInSelected(it) },
                onDismiss = onDismiss
            )
        }

        TypeNew.DIRECTORY -> {
            FileNodeDialog(
                title = "Create new Directory",
                placeholder = "name directory",
                onConfirm = { app.createDirectoryInSelected(it) },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun FileNodeDialog(
    title: String,
    placeholder: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val error = remember(key1 = textState) { validateFileName(textState.text) }

    DualButtonDialog(
        title = title,
        onConfirm = { onConfirm(textState.text.trim()) },
        onDismiss = onDismiss
    ) {
        ErrorableTextField(
            Modifier.padding(16.dp),
            placeholder,
            textState,
            { textState = it },
            error,
        )
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