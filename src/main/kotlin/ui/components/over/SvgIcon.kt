package non.era.aisdme.ui.components.over

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    tooltip: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .requiredSize(24.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = if (isHovered) LocalAisDmeColors.current.primary.copy(alpha = 0.1f)
                    else Color.Transparent,
                    shape = AisDmeShapes.small
                )
                .clip(AisDmeShapes.small)
                .clickable(onClick = onClick)
                .onPointerEvent(PointerEventType.Enter) { isHovered = true; showTooltip = true }
                .onPointerEvent(PointerEventType.Exit) { isHovered = false; showTooltip = false },
            contentAlignment = Alignment.Center
        ) {
            SvgIcon(baseName)
        }

        if (showTooltip) {
            Surface(
                modifier = Modifier
                    .offset(y = (30).dp)
                    .wrapContentSize(unbounded = true)
                    .shadow(2.dp)
                    .zIndex(10f),
                shape = RoundedCornerShape(4.dp),
                color = LocalAisDmeColors.current.surface
            ) {
                Text(
                    text = tooltip,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SvgIcon(
    baseName: String,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
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
            modifier = modifier.requiredSize(size),
            tint = Color.Unspecified // ❗ ВАЖНО
        )
    }
}