package non.era.aisdme.ui.components.over

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
            modifier = modifier.size(size),
            tint = Color.Unspecified // ❗ ВАЖНО
        )
    }
}