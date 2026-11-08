package non.era.aisdme.ui.components.custom.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.debug.debugTag
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.utils.PathHelper
import non.era.aisdme.utils.TextHelper
import java.nio.file.Path

private val PATH_PADDING = 16.dp //от текста до рамки пути
private val PATH_MIN_TEXT = 40.dp //места под текст меньше — путь не показываем, остаётся статус
private val PANEL_GAP = 4.dp //от края панели и между путём и статусом
private val STATUS_WIDTH = 76.dp
private val STATUS_RESERVED = PANEL_GAP * 2 + STATUS_WIDTH //справа от пути: зазор, статус, зазор у края

@Composable
fun BottomPanel(tab: TabCashFile, side: String) {
    Box(modifier = Modifier.fillMaxWidth().height(36.dp)) {
        //статус не вытесняется: путь получает только то, что осталось слева от него
        Path(
            tab.path, "bottom-path:$side",
            Modifier.align(Alignment.CenterStart).padding(start = PANEL_GAP, end = STATUS_RESERVED),
        )
        WriteStatus(
            tab.writeStatus, "bottom-status:$side",
            Modifier.align(Alignment.CenterEnd).padding(end = PANEL_GAP),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Path(path: Path?, tag: String, modifier: Modifier = Modifier) {
    val app = LocalAppController.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val style = LocalTextStyle.current.merge(TextStyle(fontSize = 11.sp, lineHeight = 12.sp))
    val full = path?.let { PathHelper.pathToStrFormat(it) } ?: ""

    var tooltipShown by remember { mutableStateOf(false) }
    var windowOffset by remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(Unit) { onDispose { if (tooltipShown) app.dialogController.tolltipClose() } }

    BoxWithConstraints(modifier) {
        val textWidth = maxWidth - PATH_PADDING * 2
        val textPx = with(density) { textWidth.toPx() }
        val shown = remember(full, textPx, style) {
            TextHelper.middleEllipsis(full) {
                measurer.measure(it, style, softWrap = false, maxLines = 1).size.width <= textPx
            }
        }

        if (textWidth >= PATH_MIN_TEXT) {
            Box(modifier = Modifier.height(24.dp)
                .debugTag(tag)
                .clip(AisDmeShapes.large)
                .background(LocalAisDmeColors.current.surface)
                .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.large)
                .onGloballyPositioned { windowOffset = it.localToWindow(Offset.Zero) }
                .onPointerEvent(PointerEventType.Enter) {
                    if (shown != full) { //сокращён — полный путь в подсказке
                        tooltipShown = true
                        app.dialogController.tolltipShow(full, windowOffset)
                    }
                }
                .onPointerEvent(PointerEventType.Exit) {
                    if (tooltipShown) {
                        tooltipShown = false
                        app.dialogController.tolltipClose()
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shown,
                    modifier = Modifier.padding(bottom = 1.dp).padding(horizontal = PATH_PADDING),
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    color = LocalAisDmeColors.current.onSurface
                )
            }
        }
    }
}

@Composable
private fun WriteStatus(writeStatus: WriteStatus, tag: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.width(STATUS_WIDTH).height(24.dp)
        .debugTag(tag)
        .clip(AisDmeShapes.large)
        .background(writeStatus.back)
        .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = writeStatus.text,
            modifier = Modifier.padding(bottom = 1.dp),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            color = writeStatus.te
        )
    }
}
