package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.ui.components.dialogs.elements.DualButtonDialog
import non.era.aisdme.ui.components.dialogs.elements.FileNameValidator.validateFileName
import non.era.aisdme.ui.components.dialogs.elements.GlobalMessageReq
import non.era.aisdme.ui.components.over.ErrorableTextField

@Composable
fun RenameNodeDialog(onDismiss: () -> Unit) {
    val app = LocalAppController.current
    val node = app.projectController.getSelectedNodes().singleOrNull() ?: run {
        app.dialogController.requestGlobalMessage(GlobalMessageReq.ShowGlobalError("Multi rename"))
        onDismiss()
        return
    }
    val src = node.getFile()
    val startFileName = src.toPath().fileName.toString()

    val selectionStart = startFileName.lastIndexOf('.')
    var textState by remember { mutableStateOf(
            if (selectionStart > 0) {// Выделяем от начала до последней точки (не включая точку)
                TextFieldValue(
                    text = startFileName,
                    selection = TextRange(0, selectionStart)  // Начало выделения
                )
            } else {// Если нет точки - выделяем всё имя
                TextFieldValue(startFileName, TextRange(startFileName.length))
            }
        )
    }

    val error = remember(key1 = textState) { validateFileName(textState.text) }

    DualButtonDialog(
        title = "Rename",
        onConfirm = {
            if(startFileName != textState.text)
                app.filesController.rename(src.toPath(), textState.text)
            },
        onDismiss = onDismiss,
        enterActive = { error == null }
    ) {
        ErrorableTextField(
            Modifier.padding(16.dp),
            "",
            textState,
            { textState = it },
            error,
        )
    }
}