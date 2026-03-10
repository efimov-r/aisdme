package non.era.aisdme.ui.components.custom

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScrollableContent(content: @Composable (scrollState: ScrollState) -> Unit) {
    val scrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        content(scrollState)
        VerticalScrollbar(
            modifier = modifierVerticalBuilder(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

@Composable
fun HorizontalScrollContent(content: @Composable (horizontalScrollState: ScrollState) -> Unit) {
    val horizontalScrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        content(horizontalScrollState)
        HorizontalScrollbar(
            modifier = modifierHorizontalBuilder(),
            adapter = rememberScrollbarAdapter(horizontalScrollState)
        )
    }
}

@Composable
fun ScrollableLazyContent(content: @Composable (scrollState: LazyListState) -> Unit) {
    val scrollState = rememberLazyListState()

    Box {
        content(scrollState)
        VerticalScrollbar(
            modifier = modifierVerticalBuilder(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}

private fun BoxScope.modifierVerticalBuilder() =
    Modifier
        .align(Alignment.CenterEnd)
        .fillMaxHeight()
        .width(8.dp)

private fun BoxScope.modifierHorizontalBuilder() =
    Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(8.dp)