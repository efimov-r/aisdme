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
import non.era.aisdme.di.LocalAppController

@Composable
fun BoxFocusedTree(
    content: @Composable BoxScope.(hasFocus: Boolean) -> Unit
) {
    val projController = LocalAppController.current.projectController
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(projController.treeState.inFocus) {
        if (projController.treeState.inFocus) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = Modifier
        .fillMaxHeight().padding(horizontal = 6.dp)
        .focusRequester(focusRequester)
        .onFocusChanged {
            projController.treeState.inFocus = it.isFocused
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
        content = { content(projController.treeState.inFocus) })
}