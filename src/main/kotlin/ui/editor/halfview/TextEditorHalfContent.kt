package non.era.aisdme.ui.editor.halfview

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
import non.era.aisdme.ui.editor.halfview.components.AisDmeTextField
import non.era.aisdme.ui.editor.halfview.components.BottomPanel
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.utils.PathHelper
import java.nio.file.Path

@Composable
fun TextEditorHalfContent(
    file: TabCashFileText,
    inFocus: Boolean,
    onFocusChanged: (FocusState) -> Unit,
) {
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
        BottomPanel(file.path, writeStatus)
    }
}