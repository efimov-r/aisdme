package non.era.aisdme.ui.editor.halfview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.unit.dp
import non.era.aisdme.ui.components.custom.editor.AisDmeTextField
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashPaste

@Composable
fun FromClipboardHalfContent(
    file: TabCashPaste,
    inFocus: Boolean,
    onFocusChanged: (FocusState) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 1.dp)
    ) {
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
            readOnly = true,
            onValueChange = { }
        )
    }
}