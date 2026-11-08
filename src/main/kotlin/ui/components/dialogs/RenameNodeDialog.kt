package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.files.work.FilesExecutor
import non.era.aisdme.i18n.I18n
import non.era.aisdme.ui.components.dialogs.elements.DualButtonDialog
import non.era.aisdme.ui.components.dialogs.elements.FileNameValidator.validateFileName
import non.era.aisdme.ui.components.over.ErrorableTextField
import non.era.aisdme.ui.project.data.ProjectNode
import java.nio.file.Path

@Composable
fun RenameNodeDialog(node: ProjectNode, onDismiss: () -> Unit) {
    val app = LocalAppController.current
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

    val error = remember(key1 = textState) {
        validateFileName(textState.text) ?: renameTargetValidate(src.toPath(), textState.text)
    }

    DualButtonDialog(
        title = I18n.s.rename,
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

private fun renameTargetValidate(src: Path, name: String): String? =
    runCatching {
        if (FilesExecutor.isTargetTaken(src, src.resolveSibling(name))) I18n.s.alreadyExists else null
    }.getOrElse { I18n.s.invalidName }