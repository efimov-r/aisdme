package non.era.aisdme.ui.editor.halfview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import non.era.aisdme.i18n.I18n
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.editor.ImageViewer
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileImg
import kotlin.math.roundToInt

@Composable
fun HalfContentFileImg(
    file: TabCashFileImg,
    inFocus: Boolean,
    onFocusChanged: (Boolean) -> Unit,
) {
    when (file.writeStatus) {
        WriteStatus.LOAD -> {
            Text(
                text = I18n.s.contentLoading,
                color = LocalAisDmeColors.current.onBackground,
                fontSize = 14.sp
            )
        }
        WriteStatus.READ_ONLY_FS -> {
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(inFocus) {
                if (inFocus) {
                    focusRequester.requestFocus()
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
            ) {
                val bitm = file.imgBitmap!!
                ImageStatusBar(
                    bitm,
                    file.imageViewerState.scale,
                    Modifier.align(Alignment.TopEnd).zIndex(999f)
                )
                ImageViewer(
                    bitm,
                    Modifier.fillMaxSize(),
                    file.imageViewerState,
                    focusRequester,
                    onFocusChanged
                )
            }
        }
        else -> {
            Text(
                text = I18n.s.contentNotLoaded,
                color = LocalAisDmeColors.current.onBackground,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ImageStatusBar(
    bitmap: ImageBitmap,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val percent = (scale * 100).roundToInt()

    Row(
        modifier
            .background(LocalAisDmeColors.current.surface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            "$percent%",
            color = LocalAisDmeColors.current.onSurface,
            fontSize = 12.sp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "${bitmap.width}x${bitmap.height}",
            color = LocalAisDmeColors.current.onSurface,
            fontSize = 12.sp
        )
    }
}