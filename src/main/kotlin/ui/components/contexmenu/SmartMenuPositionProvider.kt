package non.era.aisdme.ui.components.contexmenu

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

class SmartMenuPositionProvider(
    private val clickPosition: Offset,
    private val bias: Int = 0
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {

        var x = clickPosition.x.toInt()
        var y = clickPosition.y.toInt()

        if (x + popupContentSize.width > windowSize.width) {
            x -= popupContentSize.width
        }

        if (y + popupContentSize.height > windowSize.height) {
            y -= popupContentSize.height - bias
        } else {
            y += bias
        }

        return IntOffset(maxOf(0, x), maxOf(0, y))
    }
}