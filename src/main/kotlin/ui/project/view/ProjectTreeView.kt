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
import non.era.aisdme.LocalAppController
import non.era.aisdme.ui.components.contexmenu.realisation.ProjectTreeContextMenu
import non.era.aisdme.ui.components.custom.BoxFocusedAfterClick
import non.era.aisdme.ui.components.custom.DialogableElement
import non.era.aisdme.ui.components.custom.ScrollableLazyContent
import non.era.aisdme.ui.components.dialogs.DeleteSelectedNodeDialog
import non.era.aisdme.ui.project.ProjectListLineHelper

@Composable
fun ProjectTreeView(modifier: Modifier = Modifier) {
    val app = LocalAppController.current

    DialogableElement({ DeleteSelectedNodeDialog(app, it) }) { showDialog ->
        ScrollableLazyContent { scrollState->
            ProjectTreeContextMenu {
                Box(modifier = modifier.treeKeyEvent(app, showDialog)) {
                    BoxFocusedAfterClick{ hasFocus->
                        ListProjectNodes(scrollState, hasFocus)
                    }
                }
            }
        }
    }
}

@Composable
fun ListProjectNodes(scrollState: LazyListState, hasFocus: Boolean) {
    val app = LocalAppController.current

    val visibleNodes by remember {
        derivedStateOf {
            ProjectListLineHelper.buildLinearExpandedNodes(app.getNodes())
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.clipToBounds()
    ) {
        items(
            visibleNodes,
            key = {it.getFile().absolutePath}
        ) { node ->
            ProjectTreeNode(hasFocus, node)
        }
    }
}