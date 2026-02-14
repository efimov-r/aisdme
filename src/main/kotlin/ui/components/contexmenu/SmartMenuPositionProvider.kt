package non.era.aisdme.ui.components.contexmenu

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import non.era.aisdme.Log

class SmartMenuPositionProvider(
    private val clickPosition: Offset
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {

        var x = clickPosition.x.toInt()
        var y = clickPosition.y.toInt()

        // Проверяем правый край
        if (x + popupContentSize.width > windowSize.width) {
            Log.d("1")
            x -= popupContentSize.width
        }

        // Проверяем нижний край
        if (y + popupContentSize.height > windowSize.height) {
            Log.d("2")
            y -= popupContentSize.height
        }

        // Защита от отрицательных координат
        x = maxOf(0, x)
        y = maxOf(0, y)

        return IntOffset(x, y)
    }
}