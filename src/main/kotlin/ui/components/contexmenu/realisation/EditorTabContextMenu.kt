package non.era.aisdme.ui.components.contexmenu.realisation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import non.era.aisdme.di.AppController
import non.era.aisdme.ui.components.contexmenu.ContextMenu
import non.era.aisdme.ui.components.contexmenu.ContextMenuState
import non.era.aisdme.ui.components.contexmenu.MenuEntry
import non.era.aisdme.ui.editor.HalfEditorTabs
import non.era.aisdme.ui.editor.ObservableOpenedFile
import non.era.aisdme.ui.editor.Panels

@Composable
fun EditorTabContextMenu(
    half: HalfEditorTabs,
    oppositeHalf: HalfEditorTabs,
    tab: ObservableOpenedFile,
    content: @Composable BoxScope.() -> Unit
) {
    val menuState = remember { ContextMenuState() }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(Modifier
        .onGloballyPositioned { coords = it }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()

                    if (
                        event.type == PointerEventType.Press &&
                        event.buttons.isSecondaryPressed
                    ) {
                        val local = event.changes.first().position
                        val windowPos = coords?.localToWindow(local)

                        windowPos?.let {
                            menuState.show(it)
                        }
                    }
                }
            }
        }
    ) {
        content()

        if (menuState.isVisible) {
            TabContextMenu(
                state = menuState,
                half = half,
                oppositeHalf = oppositeHalf,
                tab = tab,
                onDismiss = { menuState.hide() }
            )
        }
    }
}

@Composable
private fun TabContextMenu(
    state: ContextMenuState,
    half: HalfEditorTabs,
    oppositeHalf: HalfEditorTabs,
    tab: ObservableOpenedFile,
    onDismiss: () -> Unit
) {
    buildList {
        add(MenuEntry.Item("Close") {
            half.closeTab(tab.id)
        })
        add(MenuEntry.Item("Close Other Tabs") {
            half.closeOtherTabs(tab.id)
        })
        add(MenuEntry.Item("Close All Tabs") {
            half.closeAllTabs()
        })
        add(MenuEntry.Item("Close Tabs to the Left") {
            half.closeLeftTabs(tab.id)
        })
        add(MenuEntry.Item("Close Tabs to the Right") {
            half.closeRightTabs(tab.id)
        })

        add(MenuEntry.Divider)

        add(MenuEntry.Item("Split and Move Other Half") {
            half.closeTab(tab.id)
            oppositeHalf.activateTab(tab.id)
        })
    }.let {
        ContextMenu(
            entries = it,
            state = state,
            onDismiss = onDismiss
        )
    }
}

private fun openInLeftItem(app: AppController) = MenuEntry.Item(
    "Open in Left"
) { app.openSelectedNodesInEditor(Panels.LEFT) }