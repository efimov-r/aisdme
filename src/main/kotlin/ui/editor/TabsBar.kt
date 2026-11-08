package non.era.aisdme.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import non.era.aisdme.debug.debugTag
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.contexmenu.realisation.EditorTabContextMenu
import non.era.aisdme.ui.components.custom.editor.TabMenuView
import non.era.aisdme.ui.components.over.SvgButton

private val FADE_WIDTH = 20.dp //часть затемнения у кнопки меню, которая лежит над вкладками

@Composable
fun TabsBar(
    half: HalfEditorTabs,
    oppositeHalf: HalfEditorTabs,
    modifier: Modifier = Modifier,
) {
    val editor = LocalAppController.current.editorController
    val colors = LocalAisDmeColors.current
    val listState = rememberLazyListState()
    val side = if (half === editor.leftTabs) "left" else "right"

    var menuButtonOffset by remember { mutableStateOf(Offset.Zero) }
    var tabMenuShown by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomStart
    ) {
        val tabs by remember(half) { //без ключа после rightToLeftHalf смотрит в старую половину
            derivedStateOf {
                half.listTabs.mapNotNull { tId -> editor.getTab(tId) }.toList()
            }
        }

        LaunchedEffect(listState, half) { //панель сузилась: активная вкладка должна остаться видна
            snapshotFlow { listState.layoutInfo.viewportSize.width }
                .filter { it > 0 }
                .distinctUntilChanged()
                .collectLatest {
                    val index = tabs.indexOfFirst { tab -> tab.id == half.activeTabId }
                    if (index >= 0) listState.showItem(index)
                }
        }

        if(tabMenuShown) {
            TabMenuView(menuButtonOffset, half) {
                tabMenuShown = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(50.dp)
                .align(Alignment.CenterEnd)
                .zIndex(1001f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.surface,
                            colors.surface
                        ),
                        startX = 0f,
                        endX = Float.POSITIVE_INFINITY
                    )
                )
        )

        SvgButton(
            baseName = "menu",
            modifier = Modifier
                .align(Alignment.CenterEnd).zIndex(1002f)
                .padding(end = 6.dp)
                .onGloballyPositioned { coordinates ->
                    menuButtonOffset = coordinates.localToWindow(Offset.Zero)
                },
            onClick = { tabMenuShown = true }
        )

        val overlap = 12.dp

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(-overlap),
            contentPadding = PaddingValues(end = FADE_WIDTH), //последняя вкладка не уходит под затемнение у кнопки меню
            state = listState,
            modifier = Modifier
                .padding(end = 30.dp)
                .fillMaxWidth()
                .debugTag("tabs:$side")
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
                EditorTabContextMenu(
                    modifier = Modifier.zIndex(if (tab.id == half.activeTabId) 100f else index.toFloat()),
                    half, oppositeHalf, tab
                ) {
                    EditorTab(
                        tab,
                        half.inFocus,
                        tab.id == half.activeTabId,
                        {
                            half.activateTab(it)
                            half.inFocus = true
                        },
                        { half.closeTab(it) }
                    )
                }
            }
        }
    }
}

//прокрутить так, чтобы вкладка была видна целиком; уже видна — не трогать
private suspend fun LazyListState.showItem(index: Int) {
    val info = layoutInfo
    val beyondEnd = info.visibleItemsInfo.lastOrNull()?.let { index > it.index } ?: false
    if (info.visibleItemsInfo.none { it.index == index }) scrollToItem(index) //вне экрана вкладки нет в раскладке

    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    val start = layoutInfo.viewportStartOffset + layoutInfo.beforeContentPadding
    val end = layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding
    val shift = tabScrollShift(item.offset, item.size, start, end, beyondEnd)

    if (shift != 0) scrollBy(shift.toFloat())
}

/** Сдвиг панели, чтобы вкладка влезла между `start` и `end`: плюс — вправо, минус — влево, 0 — уже видна */
internal fun tabScrollShift(offset: Int, size: Int, start: Int, end: Int, beyondEnd: Boolean): Int {
    val overEnd = offset + size - end
    //справа: к правому краю, ушедшая из раскладки справа — тоже; слева: к левому
    return if (beyondEnd || overEnd > 0) overEnd else minOf(offset - start, 0)
}