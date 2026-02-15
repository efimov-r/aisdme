package non.era.aisdme.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import non.era.aisdme.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.DragailableTreeElement
import non.era.aisdme.ui.components.custom.ProjectNodeClick

@Composable
fun ProjectTreeNode(
    hasFocus: Boolean,
    node: ProjectNode
) {
    val app = LocalAppController.current
    val isSelected = node.isSelected()

    val isDropTarget = if(app.getDragState().draggingNode) {
        app.getNodeBounds()[node]?.contains(app.getDragState().dragPosition) == true
    } else {
        false
    }

    val text = when(node) {
        is DirNode -> {
            if (node.expanded.value) "📂 ${node.getFileName()}" else "📁 ${node.getFileName()}"
        }
        is FileNode -> {
            "📄 ${node.getFileName()}"
        }
        is LinkNode -> {
            "L: ${node.getFileName()}"
        }
        else -> ""
    }

    DragailableTreeElement(app, node) {
        ProjectNodeClick(node, app) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            isSelected && hasFocus ->
                                LocalAisDmeColors.current.primary.copy(alpha = 0.5f)

                            isSelected && !hasFocus ->
                                LocalAisDmeColors.current.selectedRow.copy(alpha = 0.5f)

                            isDropTarget ->
                                LocalAisDmeColors.current.primary.copy(alpha = 0.2f)

                            else -> Color.Transparent
                        }
                    )
                    .padding(start = (node.depth * 16).dp)
            ) {
                Text(color = if(node.isCutted) LocalAisDmeColors.current.cutted else Color.Unspecified,
                    text = text)
            }
        }
    }
}