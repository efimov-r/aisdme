package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.ui.components.dialogs.elements.DualButtonDialog
import non.era.aisdme.ui.components.over.ErrorableTextField
import non.era.aisdme.ui.project.data.DirNode
import java.io.File

@Composable
fun PasteDialog(onDismiss: () -> Unit) {
    val app = LocalAppController.current
    val selected = remember { app.getSelectedNodes() }


    if(selected.isEmpty()) {
        app.requestGlobalMessage(GlobalMessageReq.ShowGlobalError("Directory for paste is not selected"))
        onDismiss()
        return
    }
    if(selected.size != 1) {
        app.requestGlobalMessage(GlobalMessageReq.ShowGlobalError("Pasting is only available in one directory."))
        onDismiss()
        return
    }
    val node = selected[0]
    if(node !is DirNode) {
        app.requestGlobalMessage(GlobalMessageReq.ShowGlobalError("Pasting is only available in directory."))
        onDismiss()
        return
    }

    var textState by remember { mutableStateOf(
        TextFieldValue(text = node.getFile().path, selection = TextRange(node.getFile().path.length))
    ) }
    val error = remember(key1 = textState) {
        pasteDialogTargetDirValidate(textState.text)
    }

    DualButtonDialog(
        title = "Paste",
        onConfirm = {
            if(error == null) app.pasteFromClipboard(File(textState.text).toPath() )
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

private fun pasteDialogTargetDirValidate(text: String): String? {
    try{
        val f = File(text)
        if(!f.exists()) return "not found"
        if(!f.isDirectory) return "its not directory"
        return null
    } catch (_: Throwable) {
        return "not found"
    }
}