package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScrollableContent(content: @Composable (scrollState: ScrollState) -> Unit) {
    val scrollState = rememberScrollState()

    Box {
        content(scrollState)
        VerticalScrollbar(
            modifier = modifierBuilder(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
fun ScrollableLazyContent(content: @Composable (scrollState: LazyListState) -> Unit) {
    val scrollState = rememberLazyListState()

    Box {
        content(scrollState)
        VerticalScrollbar(
            modifier = modifierBuilder(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

private fun BoxScope.modifierBuilder() =
    Modifier
        .align(Alignment.CenterEnd)
        .fillMaxHeight()
        .width(8.dp)