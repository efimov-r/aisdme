package non.era.aisdme.ui.editor.halfview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.editor.AisDmeTextField
import non.era.aisdme.ui.components.custom.editor.BottomPanel
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus

@Composable
fun HalfContentFileText(
    file: TabCashFileText,
    inFocus: Boolean,
    onFocusChanged: (FocusState) -> Unit,
) {
    if (file.writeStatus == WriteStatus.LOAD) {
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