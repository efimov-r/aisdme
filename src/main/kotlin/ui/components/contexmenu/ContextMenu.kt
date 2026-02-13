package non.era.aisdme.ui.components.contexmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import non.era.aisdme.Hotkeys
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.theme.dialogBorder
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

//todo положение на экране чтобы не вылазило
@Composable
fun ContextMenu(
    entries: List<MenuEntry>,
    position: Offset,
    onDismiss: () -> Unit
) {
    Popup(
        offset = IntOffset(position.x.toInt(), position.y.toInt()),
        onDismissRequest = onDismiss,
        focusable = true
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(
                    color = colors.surface,
                    shape = AisDmeShapes.medium
                )
                .dialogBorder()
                .padding(vertical = 6.dp)
        ) {
            entries.forEach { entry ->
                when (entry) {
                    is MenuEntry.Item -> MenuItem(entry, onDismiss)
                    MenuEntry.Divider -> MenuDivider()
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    item: MenuEntry.Item,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable {
                onDismiss()
                item.onClick()
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        item.icon?.let {
            SvgIcon(it, Modifier.size(16.dp))
        }

        Spacer(Modifier.width(10.dp))

        // Название
        Text(
            text = item.title,
            modifier = Modifier.weight(1f).padding(bottom = 4.dp),
            color = colors.onSurface,
            fontSize = 13.sp
        )

        // Хоткей
        item.shortcut?.let {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = it.vis,
                color = colors.onSurface.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MenuDivider() {
    Divider(
        modifier = Modifier.padding(vertical = 6.dp),
        thickness = 1.dp,
        color = LocalAisDmeColors.current.surfaceBorder.copy(alpha = 0.4f)
    )
}

@Composable
fun RightClickArea(
    menuEntries: () -> List<MenuEntry>,
    content: @Composable () -> Unit
) {
    var menuPosition by remember { mutableStateOf<Offset?>(null) }

    Box(
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (
                        event.type == PointerEventType.Press &&
                        event.buttons.isSecondaryPressed
                    ) {
                        menuPosition = event.changes.first().position
                    }
                }
            }
        }
    ) {
        content()

        menuPosition?.let { pos ->
            ContextMenu(
                entries = menuEntries(),
                position = pos,
                onDismiss = { menuPosition = null }
            )
        }
    }
}