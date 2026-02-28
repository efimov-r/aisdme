package non.era.aisdme.ui.components.over

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors

object IconRepository {

    private val cache = mutableMapOf<String, Painter>()

    fun get(
        path: String,
        density: Density
    ): Painter? {
        return cache[path] ?: run {
            val stream = IconRepository::class
                .java
                .getResourceAsStream(path)
                ?: return null

            val painter = loadSvgPainter(stream, density)
            cache[path] = painter
            painter
        }
    }
}

@Composable
fun themedIconPath(base: String): String {
    val dark = isSystemInDarkTheme()
    return if (dark) {
        "/icons/${base}/${base}_dark.svg"
    } else {
        "/icons/${base}/${base}.svg"
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SvgButton(
    baseName: String,
    tooltip: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val app = LocalAppController.current

    var isHovered by remember { mutableStateOf(false) }
    var windowOffset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier
        .requiredSize(24.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusProperties { canFocus = false }
                .background(
                    color = if (isHovered) LocalAisDmeColors.current.primary.copy(alpha = 0.1f)
                    else Color.Transparent,
                    shape = AisDmeShapes.small
                )
                .clip(AisDmeShapes.small)
                .onGloballyPositioned { coordinates ->
                    // Получаем позицию Box относительно окна
                    windowOffset = coordinates.localToWindow(Offset.Zero)
                }
                .clickable(onClick = onClick)
                .onPointerEvent(PointerEventType.Enter) {
                    if(tooltip != null) {
                        isHovered = true
                        app.dialogController.tolltipShow(tooltip, windowOffset)
                    }
                }
                .onPointerEvent(PointerEventType.Exit) {
                    isHovered = false
                    app.dialogController.tolltipClose()
               },
            contentAlignment = Alignment.Center
        ) {
            SvgIcon(baseName)
        }
    }
}

@Composable
fun SvgIcon(
    baseName: String,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val path = themedIconPath(baseName)

    val painter = remember(path, density) {
        IconRepository.get(path, density)
    }

    if (painter != null) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = modifier,
            tint = Color.Unspecified // ❗ ВАЖНО
        )
    }
}