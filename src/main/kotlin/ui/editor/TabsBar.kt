package non.era.aisdme.ui.editor

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import non.era.aisdme.di.LocalAppController

@Composable
fun TabsBar(
    half: HalfEditorTabs,
    modifier: Modifier = Modifier,
) {
    val editor = LocalAppController.current.editorController
    val listState = rememberLazyListState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomStart
    ) {
        val tabs by remember {
            derivedStateOf {
                half.listTabs.mapNotNull { tId -> editor.getFile(tId) }.toList()
            }
        }

        val overlap = 12.dp

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(-overlap),
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    coroutineScope {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val delta = event.changes.last().scrollDelta.y

                                if (delta != 0f) {
                                    launch {
                                        listState.scrollBy(delta * 40f)
                                    }
                                }
                            }
                        }
                    }
                },
            userScrollEnabled = true
        ) {
            itemsIndexed(tabs, key = { _, tab -> tab.id }) { index, tab ->

                EditorTab(
                    tab,
                    half.inFocus,
                    tab.id == half.activeTabId,
                    {
                        half.activateTab(it)
                        half.inFocus = true
                    },
                    { half.closeTab(it) },
                    modifier = Modifier
                        .zIndex(if (tab.id == half.activeTabId) 100f else index.toFloat())
                )
            }
        }
    }
}