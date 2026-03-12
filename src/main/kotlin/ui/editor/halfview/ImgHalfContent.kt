package non.era.aisdme.ui.editor.halfview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.editor.halfview.components.BottomPanel
import non.era.aisdme.ui.editor.halfview.components.ImageViewer
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileImg

@Composable
fun ImgHalfContent(
    file: TabCashFileImg,
    inFocus: Boolean,
    onFocusChanged: (Boolean) -> Unit,
) {
    val writeStatus = file.writeStatus

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 1.dp)
            ) {
                when (writeStatus) {
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

        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(LocalAisDmeColors.current.surfaceBorder))
        BottomPanel(file.path, writeStatus)
    }
}