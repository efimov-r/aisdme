package non.era.aisdme.ui.components.dialogs.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.over.NodeIcon
import non.era.aisdme.ui.project.data.ProjectNode

@Composable
fun DialogNode(
    node: ProjectNode,
    itemHeight: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NodeIcon(Modifier.padding(top = 2.dp, end = 4.dp), node)

        Text(
            color = LocalAisDmeColors.current.onSurface,
            text = "${node.getFileName()}",
            style = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 26.sp),
            maxLines = 1,
            softWrap = false,
        )
    }
}

