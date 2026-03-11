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
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.ui.components.contexmenu.ContextMenu
import non.era.aisdme.ui.components.contexmenu.ContextMenuState
import non.era.aisdme.ui.components.contexmenu.MenuEntry
import non.era.aisdme.ui.editor.HalfEditorTabs
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.editor.halfviewmodel.TabCashTextFile

@Composable
fun EditorTabContextMenu(
    modifier: Modifier = Modifier,
    half: HalfEditorTabs,
    oppositeHalf: HalfEditorTabs,
    tab: TabCash,
    content: @Composable BoxScope.() -> Unit
) {
    val menuState = remember { ContextMenuState() }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(modifier
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
    tab: TabCash,
    onDismiss: () -> Unit
) {
    val editController = LocalAppController.current.editorController

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

        add(MenuEntry.Item("Split Other Half") {
            oppositeHalf.activateTab(tab.id)
        })

        add(MenuEntry.Item("Split and Move Other Half") {
            half.closeTab(tab.id)
            oppositeHalf.activateTab(tab.id)
            oppositeHalf.inFocus = true
        })

        if(tab is TabCashTextFile) {
            add(MenuEntry.Divider)

            add(MenuEntry.Item("Show in Tree") {
                editController.showInTree(tab.id)
            })
        }
    }.let {
        ContextMenu(
            entries = it,
            state = state,
            onDismiss = onDismiss
        )
    }
}