package non.era.aisdme.ui.project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.DragailableTreeElement
import non.era.aisdme.ui.components.custom.ProjectNodeClick
import non.era.aisdme.ui.components.over.NodeIcon
import non.era.aisdme.ui.components.over.SvgIcon
import non.era.aisdme.ui.project.TypeClickNode
import non.era.aisdme.ui.project.data.DirNode
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.utils.PathHelper

@Composable
fun ProjectTreeNode(
    hasFocus: Boolean,
    node: ProjectNode
) {
    val app = LocalAppController.current
    val treeState = app.projectController.treeState
    val isSelected by remember(node) { derivedStateOf { treeState.selection.isSelected(node) } } //только свой узел
    val isCut by remember(node) { derivedStateOf { treeState.isCut(node) } }

    val isDropTarget by remember(node) { derivedStateOf { app.moveController.isDropTarget(node) } } //только свой узел

    DragailableTreeElement(app, node) {
        ProjectNodeClick(node, app) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            isSelected && hasFocus ->
                                LocalAisDmeColors.current.selectAndFocus

                            isSelected && !hasFocus ->
                                LocalAisDmeColors.current.selectAndNotFocus

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
                                    app.projectController.clickNode(node, TypeClickNode.CHEVRON)
                            }
                        }
                    }
                ) {
                    if(node is DirNode && !node.expanded)
                        SvgIcon("chevronRight", Modifier.padding(top = 2.dp).align(Alignment.CenterStart).requiredSize(20.dp))
                    if(node is DirNode && node.expanded)
                        SvgIcon("chevronDown", Modifier.padding(top = 2.dp).align(Alignment.CenterStart).requiredSize(20.dp))
                }
                Box(Modifier
                    .width(1.dp).height(1.dp)
                    .onGloballyPositioned { coords ->
                        val position = coords.boundsInWindow()
                        treeState.setMenuAnchor(node, Offset(position.left, position.bottom))
                    })
                NodeIcon(Modifier.padding(top = 2.dp, end = 4.dp), node)

                Text(
                    color = if(isCut) LocalAisDmeColors.current.cutted else LocalAisDmeColors.current.onSurface,
                    text = "${node.getFileName()}",
                    style = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 26.sp),
                    maxLines = 1,
                    softWrap = false,
                )

                val rootProj = app.winState.projectRoot
                if(node.depth == 0 && rootProj != null) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        color = LocalAisDmeColors.current.cutted,
                        text = PathHelper.pathToStrFormat(rootProj),
                        style = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 26.sp),
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}