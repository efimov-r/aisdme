package non.era.aisdme.ui.components.contexmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.debug.debugTag
import non.era.aisdme.settings.Hotkeys
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.over.SvgIcon

sealed class MenuEntry {
    data class Item(
        val title: String,
        val icon: String? = null,
        val shortcut: Hotkeys? = null,
        val onClick: () -> Unit
    ) : MenuEntry()

    object Divider : MenuEntry()
}

@Composable
fun MenuItem(
    item: MenuEntry.Item,
    selected: Boolean,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .debugTag("menu:${item.title}")
            .clickable {
                onDismiss()
                item.onClick()
            }
            .background(
                color = if(selected) LocalAisDmeColors.current.menuSelected else Color.Transparent
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (item.icon != null) {
            SvgIcon(item.icon, Modifier.size(16.dp))
        } else {
            Spacer(Modifier.width(16.dp))
        }

        Spacer(Modifier.width(10.dp))

        Text( // Название
            text = item.title,
            modifier = Modifier.weight(1f).padding(bottom = 4.dp),
            color = LocalAisDmeColors.current.onSurface,
            fontSize = 13.sp
        )

        item.shortcut?.let { // Хоткей
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = it.vis,
                color = LocalAisDmeColors.current.textSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MenuDivider() {
    Divider(
        modifier = Modifier.padding(vertical = 6.dp),
        thickness = 1.dp,
        color = LocalAisDmeColors.current.menuDivider
    )
}