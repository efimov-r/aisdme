package non.era.aisdme.ui.components.contexmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import non.era.aisdme.settings.Hotkeys
import non.era.aisdme.theme.AisDmeColors
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.theme.dialogBorder
import non.era.aisdme.ui.components.over.SvgIcon
import non.era.aisdme.utils.Log

@Composable
fun ContextMenu(
    entries: List<MenuEntry>,
    state: ContextMenuState,
    onDismiss: () -> Unit
) {
    val items = remember { entries.filterIsInstance<MenuEntry.Item>() }
    var selectedIndex by remember { mutableStateOf(-1) }

    var isPopupReady by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Popup(
        popupPositionProvider = SmartMenuPositionProvider(state.position),
        onDismissRequest = onDismiss,
        focusable = true
    ) {
        Column(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable()
                .onGloballyPositioned {
                    isPopupReady = true
                }
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                    when (event.key) {
                        Key.DirectionDown -> {
                            selectedIndex = (selectedIndex + 1) % items.size
                            true
                        }

                        Key.DirectionUp -> {
                            selectedIndex =
                                if (selectedIndex <= 0) items.lastIndex else selectedIndex - 1
                            true
                        }

                        Key.Enter -> {
                            if(selectedIndex != -1) {
                                items[selectedIndex].onClick()
                                onDismiss()
                            }
                            true
                        }

                        Key.Escape -> {
                            onDismiss()
                            true
                        }

                        else -> false
                    }
                }
                .width(300.dp)
                .background(
                    color = LocalAisDmeColors.current.surface,
                    shape = AisDmeShapes.medium
                )
                .dialogBorder()
                .padding(vertical = 6.dp)
        ) {
            MenuItems(entries, selectedIndex, onDismiss)
        }
    }

    LaunchedEffect(isPopupReady) {
        if(isPopupReady) focusRequester.requestFocus()
    }
}

@Composable
private fun MenuItems(
    entries: List<MenuEntry>,
    selectedIndex: Int,
    onDismiss: () -> Unit
) {
    var itemIndex = 0

    entries.forEach { entry ->
        when (entry) {
            is MenuEntry.Item -> {
                MenuItem(
                    entry,
                    itemIndex == selectedIndex,
                    onDismiss
                )
                itemIndex++
            }

            MenuEntry.Divider -> MenuDivider()
        }
    }
}