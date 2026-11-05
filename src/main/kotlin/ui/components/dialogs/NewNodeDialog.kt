package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.ui.components.dialogs.elements.DualButtonDialog
import non.era.aisdme.ui.components.dialogs.elements.FileNameValidator.validateFileName
import non.era.aisdme.ui.components.over.ErrorableTextField
import java.nio.file.Path

enum class TypeNew {
    FILE, DIRECTORY
}

@Composable
fun NewNodeDialog(
    typeNew: TypeNew,
    dir: Path,
    onDismiss: () -> Unit
) {
    val app = LocalAppController.current
    when (typeNew) {
        TypeNew.FILE -> {
            FileNodeDialog(
                title = "Create new File",
                placeholder = "filename",
                onConfirm = { app.filesController.createFile(dir, it) },
                onDismiss = onDismiss
            )
        }

        TypeNew.DIRECTORY -> {
            FileNodeDialog(
                title = "Create new Directory",
                placeholder = "name directory",
                onConfirm = { app.filesController.createDirectory(dir, it) },
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
        onDismiss = onDismiss,
        enterActive = { error == null }
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