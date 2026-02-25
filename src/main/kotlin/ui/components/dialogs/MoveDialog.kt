package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.ui.components.dialogs.elements.DualButtonDialog
import non.era.aisdme.ui.components.dialogs.elements.GlobalMessageReq
import non.era.aisdme.ui.components.over.ErrorableTextField
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

@Composable
fun MoveDialog(src: List<ProjectNode>, target: File, onDismiss: () -> Unit) {
    val app = LocalAppController.current
    if(src.isEmpty()) {
        app.requestGlobalMessage(GlobalMessageReq.ShowGlobalError("Selected file to move does not exist."))
        onDismiss()
        return
    }

    var textState by remember { mutableStateOf(
        TextFieldValue(text = target.path, selection = TextRange(target.path.length))
    ) }
    val error = remember(key1 = textState) {
        dialogTargetDirValidate(src, textState.text)
    }

    DualButtonDialog(
        title = "Move",
        onConfirm = {
            if(error == null) app.move(src, File(textState.text))
        },
        onDismiss = onDismiss
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
        if(!f.exists()) return "not found"
        if(!f.isDirectory) return "its not directory"
        src.forEach {
            val srcFile = it.getFile()
            if (srcFile.isDirectory && f.toPath().startsWith(srcFile.toPath())) {
                return "Cannot move directory into itself"
            }
        }
        return null
    } catch (_: Throwable) {
        return "not found"
    }
}