package non.era.aisdme.ui.editor.halfview.texfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.editor.halfview.AisDmeTextField
import non.era.aisdme.ui.editor.halfviewmodel.ObservableOpenedHalfFile
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus

@Composable
fun TextEditorHalfContent(
    file: ObservableOpenedHalfFile,
    inFocus: Boolean,
    onFocusChanged: (FocusState) -> Unit,
) {
    val path = file.path ?: ""
    val writeStatus = file.writeStatus

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.weight(1f)) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 1.dp)
            ) {
                if (writeStatus == WriteStatus.LOAD) {
                    Text(
                        text = "Load ... ",
                        color = LocalAisDmeColors.current.onBackground,
                        fontSize = 14.sp
                    )
                } else {
                    val focusRequester = remember { FocusRequester() }

                    LaunchedEffect(inFocus) {
                        if (inFocus) {
                            focusRequester.requestFocus()
                        }
                    }

                    AisDmeTextField(
                        focusRequester = focusRequester,
                        onFocusChanged = onFocusChanged,
                        textFieldValue = file.textFieldValue,
                        readOnly = file.writeStatus.readOnly,
                        onValueChange = { file.updateTextFieldValue(it) }
                    )
                }
            }

        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(LocalAisDmeColors.current.surfaceBorder))
        BottomPanel(path, writeStatus)
    }
}

@Composable
private fun BottomPanel(path: String, writeStatus: WriteStatus,) {
    Row(modifier = Modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(4.dp))
        Path(path)
        Spacer(Modifier.weight(1f))
        WriteStatus(writeStatus)
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun Path(path: String) {
    Box(modifier = Modifier.height(24.dp)
        .clip(AisDmeShapes.large)
        .background(LocalAisDmeColors.current.surface)
        .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = path,
            modifier = Modifier.padding(bottom = 1.dp).padding(horizontal = 16.dp),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            color = LocalAisDmeColors.current.onSurface
        )
    }
}

@Composable
private fun WriteStatus(writeStatus: WriteStatus) {
    Box(modifier = Modifier.width(76.dp).height(24.dp)
        .clip(AisDmeShapes.large)
        .background(writeStatus.back)
        .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = writeStatus.text,
            modifier = Modifier.padding(bottom = 1.dp),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            color = writeStatus.te
        )
    }
}