package non.era.aisdme.ui.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TabsBar(
    modifier: Modifier = Modifier,
    tabs: List<ObservableOpenedFile>,
    activeId: Int?,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        tabs.forEach { tab ->
            EditorTab(tab, (tab.id == activeId), onSelect, onClose)
        }
    }
}