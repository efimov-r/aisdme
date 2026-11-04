package non.era.aisdme.ui.components.dialogs

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.dialogs.elements.DialogNode
import non.era.aisdme.ui.components.dialogs.elements.DualButtonDialog

@Composable
fun DeleteSelectedNodeDialog(onDismiss: () -> Unit) {
    val app = LocalAppController.current
    val nodes = app.projectController.treeState.getSelectedNodes()
    val title = "Delete object${if(nodes.size > 1) "s" else ""}?"
    val scrollState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val itemHeight = 32.dp
    val maxItemsBefScr = 5
    val needScroll = remember(nodes) {
        nodes.size > maxItemsBefScr
    }
    val sizeBox = remember(nodes) {
        if(nodes.size > maxItemsBefScr) maxItemsBefScr*itemHeight else  nodes.size*itemHeight
    }

    DualButtonDialog(
        Modifier.focusRequester(focusRequester).focusable(),
        title,
        { app.deleteSelected() },
        onDismiss,
        enterActive = { true }
    ) {
        if (needScroll)
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(LocalAisDmeColors.current.surfaceBorder))
        Box(Modifier.height(sizeBox).padding(start = 36.dp)) {
            LazyColumn(state = scrollState) {
                items(nodes) { node ->
                    DialogNode(node, itemHeight)
                }
            }

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(8.dp),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }
        if (needScroll)
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(LocalAisDmeColors.current.surfaceBorder))
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}