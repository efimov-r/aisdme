package non.era.aisdme.ui.project.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import non.era.aisdme.LocalAppController
import non.era.aisdme.ui.components.contexmenu.realisation.ProjectTreeContextMenu
import non.era.aisdme.ui.components.custom.BoxFocusedAfterClick
import non.era.aisdme.ui.components.custom.ScrollableLazyContent
import non.era.aisdme.ui.components.dialogs.UiDialog
import non.era.aisdme.ui.project.ProjectListLineHelper

@Composable
fun ProjectTreeView(modifier: Modifier = Modifier) {
    val app = LocalAppController.current

    ScrollableLazyContent { scrollState->
        ProjectTreeContextMenu {
            Box(modifier = modifier
                .treeKeyEvent(app, { app.requestDialog(UiDialog.ShowDeleteDialog) })
            ) {
                BoxFocusedAfterClick{ hasFocus->
                    ListProjectNodes(scrollState, hasFocus)
                }
            }
        }
    }
}

@Composable
fun ListProjectNodes(scrollState: LazyListState, hasFocus: Boolean) {
    val app = LocalAppController.current
    val moveController = app.getMoveController()
    moveController.setScrollState(scrollState)

    val visibleNodes by remember {
        derivedStateOf {
            ProjectListLineHelper.buildLinearExpandedNodes(app.getNodes())
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .clipToBounds()
            .onGloballyPositioned {
                moveController.setListBounds(it.boundsInWindow())
            }
    ) {
        items(
            visibleNodes,
            key = {it.getFile().absolutePath}
        ) { node ->
            ProjectTreeNode(hasFocus, node)
        }
    }
}