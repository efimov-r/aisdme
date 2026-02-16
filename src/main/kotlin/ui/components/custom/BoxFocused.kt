package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun BoxFocusedAfterClick(
    content: @Composable BoxScope.(hasFocus: Boolean) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    Box(modifier = Modifier
        .fillMaxHeight().padding(horizontal = 6.dp)
        .focusRequester(focusRequester)
        .onFocusChanged {
            hasFocus = it.isFocused
        }
        .focusable()
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    if (awaitPointerEvent().type == PointerEventType.Press) {
                        focusRequester.requestFocus()
                    }
                }
            }
        },
        content = { content(hasFocus) })
}