package non.era.aisdme.ui.editor

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun TabsBar(
    modifier: Modifier = Modifier,
    tabs: List<ObservableOpenedFile>,
    activeId: Int?,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomStart
    ) {
        LazyRow(
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
            itemsIndexed(
                tabs,
                key = { _, tab -> tab.id }
            ) { index, tab ->

                val isActive = tab.id == activeId

                EditorTab(
                    tab,
                    isActive,
                    onSelect,
                    onClose,
                    modifier = Modifier
                        .offset(x = (-index * 12).dp) // overlap
                        .zIndex(if (isActive) 100f else index.toFloat())
                )
            }
        }
    }
}