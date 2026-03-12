package non.era.aisdme.ui.editor.halfview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.editor.ImageViewer
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileImg

@Composable
fun HalfContentFileImg(
    file: TabCashFileImg,
    inFocus: Boolean,
    onFocusChanged: (Boolean) -> Unit,
) {
    when (file.writeStatus) {
        WriteStatus.LOAD -> {
            Text(
                text = "Load ... ",
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
                ImageViewer(
                    file.imgBitmap!!,
                    Modifier.fillMaxSize(),
                    file.imageViewerState,
                    focusRequester,
                    onFocusChanged
                )
            }
        }
        else -> {
            Text(
                text = "Not load",
                color = LocalAisDmeColors.current.onBackground,
                fontSize = 14.sp
            )
        }
    }
}