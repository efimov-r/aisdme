package non.era.aisdme.ui.project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.DragailableTreeElement
import non.era.aisdme.ui.components.custom.ProjectNodeClick
import non.era.aisdme.ui.components.over.SvgIcon
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.FileNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.ui.project.TypeClickNode

@Composable
fun ProjectTreeNode(
    hasFocus: Boolean,
    node: ProjectNode
) {
    val app = LocalAppController.current
    val isSelected = node.isSelected()

    val isDropTarget = app.getMoveController().isDropTarget(node)

    val icon: String = when(node) {
        is DirNode -> "folder"
        is FileNode -> "anyType"
        else -> "unknown"
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
                    .padding(start = (node.depth * 20).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier
                    .requiredWidth(24.dp)
                    .pointerInput(node) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if(event.type == PointerEventType.Press && event.buttons.isPrimaryPressed)
                                    app.clickNode(node, TypeClickNode.CHEVRON)
                            }
                        }
                    }
                ) {
                    if(node is DirNode && !node.expanded)
                        SvgIcon("chevronRight", Modifier.padding(top = 2.dp).align(Alignment.CenterStart), 20.dp)
                    if(node is DirNode && node.expanded)
                        SvgIcon("chevronDown", Modifier.padding(top = 2.dp).align(Alignment.CenterStart), 20.dp)
                }

                SvgIcon(icon, Modifier.padding(top = 2.dp, end = 4.dp))

                Text(
                    color = if(node.isCutted) LocalAisDmeColors.current.cutted else LocalAisDmeColors.current.onSurface,
                    text = "${node.getFileName()}",
                    style = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 26.sp),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}