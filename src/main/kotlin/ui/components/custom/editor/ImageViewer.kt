package non.era.aisdme.ui.components.custom.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.editor.halfviewmodel.help.ImageViewerState
import kotlin.math.roundToInt

@Composable
fun ImageViewer(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    state: ImageViewerState,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
) {
    val density = LocalDensity.current

    val imgW = bitmap.width.toFloat()
    val imgH = bitmap.height.toFloat()

    Box(
        modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                onFocusChanged(focusState.isFocused)
            }
            .focusable()
            .background(LocalAisDmeColors.current.surface)
            .onSizeChanged {
                state.onSizeChanged(it, imgW, imgH)
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        val scroll = change.scrollDelta

                        if(event.type == PointerEventType.Press) {
                            event.changes.firstOrNull()?.consume()
                            onFocusChanged(true)
                        }

                        if (scroll.y != 0f) {
                            if (event.keyboardModifiers.isCtrlPressed) {
                                state.handleZoom(change, scroll)
                            } else if (event.keyboardModifiers.isShiftPressed) {
                                state.offsetX -= scroll.y * 40f
                            } else {
                                state.offsetY -= scroll.y * 40f
                            }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    state.offsetX += drag.x
                    state.offsetY += drag.y
                }
            }
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                if (it.isCtrlPressed) {
                    when (it.key) {
                        Key.Equals, Key.Plus -> { state.scale *= 1.1f; true }
                        Key.Minus -> { state.scale *= 0.9f; true }
                        Key.NumPadAdd -> { state.scale *= 1.1f; true }
                        Key.NumPadSubtract -> { state.scale *= 0.9f; true }
                        else -> false
                    }
                } else false
            }
            .clipToBounds()
    ) {
        Box(
            Modifier
                .offset { IntOffset(state.offsetX.roundToInt(), state.offsetY.roundToInt()) }
                .size(
                    with(density) { (imgW * state.scale).toDp() },
                    with(density) { (imgH * state.scale).toDp() }
                )
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}