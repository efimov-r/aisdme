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
import non.era.aisdme.ui.components.over.ErrorableTextField
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

@Composable
fun MoveDialog(src: List<ProjectNode>, target: File, onDismiss: () -> Unit) {
    val app = LocalAppController.current

    var textState by remember { mutableStateOf(
        TextFieldValue(text = target.path, selection = TextRange(target.path.length))
    ) }
    val error = remember(key1 = textState) {
        dialogTargetDirValidate(src, textState.text)
    }

    DualButtonDialog(
        title = I18n.s.dialogMove,
        onConfirm = {
            app.filesController.move(src.map { it.getFilePath() }, File(textState.text).toPath())
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

private fun dialogTargetDirValidate(src: List<ProjectNode>, text: String): String? {
    try{
        val f = File(text)
        val s = I18n.s
        if(!f.exists()) return s.notFound
        if(!f.isDirectory) return s.notDirectory
        src.forEach {
            val srcFile = it.getFile()
            if (srcFile.isDirectory && f.toPath().startsWith(srcFile.toPath())) {
                return s.cannotMoveIntoItself
            }
        }
        FilesExecutor.findTakenTarget(src.map { it.getFilePath() }, f.toPath())?.let {
            return s.alreadyExists(it.fileName.toString())
        }
        return null
    } catch (_: Throwable) {
        return I18n.s.notFound
    }
}