package non.era.aisdme.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.over.NodeIcon
import non.era.aisdme.ui.components.over.SvgButton
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText

@Composable
fun EditorTab(
    tab: TabCash,
    halfInFocus: Boolean,
    isActive: Boolean,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalAppController.current
    val colors = LocalAisDmeColors.current
    val colorSurfaceBorder = colors.surfaceBorder
    val colorBack = remember(colors, isActive, halfInFocus) {
        when {
            isActive && halfInFocus -> colors.selectAndFocus
            isActive && !halfInFocus -> colors.selectAndNotFocus
            else -> colors.surface
        }
    }
    Row(
        modifier = modifier
            .padding(0.dp) //между элементами
            .clip(TrapezoidBottomShape())
            .background(colorBack)
            .drawBehind {
                val outline = TrapezoidBottomShape()
                    .createOutline(size, layoutDirection, this)

                val path = (outline as Outline.Generic).path

                drawPath(
                    path = path,
                    color = colorSurfaceBorder,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        cap = StrokeCap.Square,
                        join = StrokeJoin.Round
                    )
                )

                drawRect(// стираем нижнюю линию
                    color = colorBack,
                    topLeft = Offset(0f, size.height - 2.dp.toPx()),
                    size = Size(size.width, 4.dp.toPx())
                )
            }
            .clickable { onSelect(tab.id) }
            .padding(horizontal = 14.dp, vertical = 2.dp), //внутри элемента, от текста до границ
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(tab is TabCashFileText) {
            NodeIcon(Modifier.padding(top = 3.dp), app.projectController.treeState.findNodes(tab.getFile()))
        }
        Spacer(Modifier.width(2.dp))
        Text(
            modifier = Modifier.padding(bottom = 1.dp),
            text = tab.name ?: "Untitled",
            color = LocalAisDmeColors.current.onSurface,
            maxLines = 1,
            fontSize = 13.sp,
        )
        Spacer(Modifier.width(3.dp))
        SvgButton(
            baseName = "close",
            modifier = Modifier.padding(top = 5.dp)
        ){
            onClose(tab.id)
        }
    }
}

class TrapezoidBottomShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = with(density) {

        val expand = 10.dp.toPx() //сужение вверху
        val radius = 8.dp.toPx() //радиус скруглений
        val offset = 2f //корректировка для окружности

        val path = Path()

        val bottomLeft = Offset(0f, size.height)
        val bottomRight = Offset(size.width, size.height)

        path.moveTo(bottomLeft.x, bottomLeft.y) // старт слева внизу
        path.lineTo(expand, radius) // левая наклонная сторона
        path.quadraticBezierTo( // левый верхний угол
            expand + offset, 0f,
            expand + radius, 0f
        )

        path.lineTo(size.width - radius - expand, 0f) // верхняя линия

        path.quadraticBezierTo( // правый верхний угол
            size.width - expand - offset, 0f,
            size.width - expand, radius
        )

        path.lineTo(bottomRight.x, bottomRight.y) // правая наклонная сторона

        path.close()

        Outline.Generic(path)
    }
}