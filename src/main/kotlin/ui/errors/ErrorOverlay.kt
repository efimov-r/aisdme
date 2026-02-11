package non.era.aisdme.ui.errors

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.first
import non.era.aisdme.LocalAppController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.AisDmeSpacing
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.theme.aisDmeButtonBorder

@Composable
fun ErrorOverlay() {
    val app = LocalAppController.current
    val mess = app.globalMess

    if (mess.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f) // поверх всего
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AisDmeSpacing.l)
                .width(420.dp)
                .heightIn(max = 600.dp)
        ) {
            val listState = rememberLazyListState()
            var oldVal by remember { mutableIntStateOf(0) }

            LaunchedEffect(mess.size) {
                if (mess.isNotEmpty()) {
                    snapshotFlow { listState.layoutInfo.totalItemsCount }
                        .first { it >= mess.size }
                    if(mess.size - 1 > oldVal) {
                        oldVal = mess.size - 1
                        listState.animateScrollToItem(mess.size - 1)
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
                ,reverseLayout = true
            ) {
                items(
                    items = mess,
                    key = { it.id }
                ) { messItem ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        GlobalMessageItem(
                            modifier = Modifier.padding(bottom = AisDmeSpacing.s),
                            mess = messItem,
                            onClose = { app.removeMess(messItem.id) }
                        )
                    }
                }
            }

            Button(
                onClick = app::clearGlobalMess,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                border = aisDmeButtonBorder(),
                shape = AisDmeShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = LocalAisDmeColors.current.surface,
                    contentColor = LocalAisDmeColors.current.onSurface
                )
            ) {
                Text("Close all")
            }
        }
    }
}
