package non.era.aisdme.ui.editor.halfview.texfield

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.VerticalAutoscroll
import non.era.aisdme.ui.components.custom.horizontalAutoscroll
import non.era.aisdme.ui.components.over.LineNumbers
import non.era.aisdme.ui.editor.halfviewmodel.ObservableOpenedHalfFile

@Composable
fun AisDmeTextField(
    focusRequester: FocusRequester,
    onFocusChanged: (FocusState) -> Unit,
    file: ObservableOpenedHalfFile,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val startEditorSpace = 4.dp
    val endEditorSpace = 120.dp
    val rightEditorSpace = 20.dp
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    var viewportWidthPx by remember { mutableIntStateOf(0) }

    VerticalAutoscroll(
        layoutResult,
        viewportHeightPx,
        file.textFieldValue,
        startEditorSpace
    ) { verticalScrollState ->
        Row(
            Modifier
                .fillMaxSize()
                .onSizeChanged {
                    viewportHeightPx = it.height
                    viewportWidthPx = it.width
                }
        ) {
            LineNumbers(
                layoutResult = layoutResult,
                scrollState = verticalScrollState,
                edgePadding = startEditorSpace + 3.dp
            )

            Spacer(
                Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(LocalAisDmeColors.current.surfaceBorder)
            )

            Spacer(Modifier.width(2.dp))
            horizontalAutoscroll(layoutResult, viewportWidthPx, file.textFieldValue) { horizontalScrollState ->
                Column(
                    Modifier
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .fillMaxSize()
                ) {
                    Spacer(Modifier.height(startEditorSpace))
                    Row(
                        Modifier.fillMaxSize(),
                    ) {
                        BasicTextField(
                            value = file.textFieldValue,
                            readOnly = file.writeStatus.readOnly,
                            onValueChange = { newValue ->
                                file.updateTextFieldValue(newValue)
                            },
                            textStyle = TextStyle(
                                color = LocalAisDmeColors.current.onSurface,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    onFocusChanged(it)
                                },
                            onTextLayout = { layoutResult = it }
                        )
                        Spacer(Modifier.width(rightEditorSpace))
                    }
                    Spacer(Modifier.height(endEditorSpace))
                }
            }
        }
    }
}