package non.era.aisdme.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TabsBar(
    tabs: List<ObservableEditorTab>,
    activeId: Int?,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit
) {
    Row {
        tabs.forEach { tab ->
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .background(
                        if (tab.id == activeId) Color.DarkGray else Color.Gray
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tab.title,
                    modifier = Modifier.clickable { onSelect(tab.id) }
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "✕",
                    modifier = Modifier.clickable { onClose(tab.id) }
                )
            }
        }
    }
}