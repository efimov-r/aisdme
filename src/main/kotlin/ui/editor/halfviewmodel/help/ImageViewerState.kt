package non.era.aisdme.ui.editor.halfviewmodel.help

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

class ImageViewerState(
    scaleInit: Float = 1f,
    offsetXInit: Float = 0f,
    offsetYInit: Float = 0f,
    viewportSizeInit: IntSize = IntSize.Zero,
) {
    var scale by mutableStateOf(scaleInit)
    var offsetX by mutableStateOf(offsetXInit)
    var offsetY by mutableStateOf(offsetYInit)
    var viewportSize by mutableStateOf(viewportSizeInit)

    fun copy() =
        ImageViewerState( scale, offsetX, offsetY, viewportSize)

    fun handleZoom(change: PointerInputChange, scroll: Offset) {
        val zoomFactor = if (scroll.y < 0) 1.1f else 0.9f
        val mouse = change.position

        val newScale = scale * zoomFactor
        val scaleChange = newScale / scale

        offsetX = (offsetX - mouse.x) * scaleChange + mouse.x
        offsetY = (offsetY - mouse.y) * scaleChange + mouse.y
        scale = newScale
    }

    fun onSizeChanged(newSize: IntSize, imgW: Float, imgH: Float) {
        val oldSize = viewportSize
        viewportSize = newSize
        if (oldSize == IntSize.Zero) {  // Только при первом измерении
            val fit = min(
                viewportSize.width / imgW,
                viewportSize.height / imgH
            )
            scale = fit
            offsetX = 0f
            offsetY = 0f
        }
    }
}