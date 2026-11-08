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
import non.era.aisdme.i18n.I18n
import non.era.aisdme.ui.components.contexmenu.ContextMenu
import non.era.aisdme.ui.components.contexmenu.ContextMenuState
import non.era.aisdme.ui.components.contexmenu.MenuEntry
import non.era.aisdme.ui.editor.HalfEditorTabs
import non.era.aisdme.ui.editor.halfviewmodel.TabCash
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText

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
    val s = I18n.s

    buildList {
        add(MenuEntry.Item(s.tabClose) {
            half.closeTab(tab.id)
        })
        add(MenuEntry.Item(s.tabCloseOthers) {
            half.closeOtherTabs(tab.id)
        })
        add(MenuEntry.Item(s.tabCloseAll) {
            half.closeAllTabs()
        })
        add(MenuEntry.Item(s.tabCloseLeft) {
            half.closeLeftTabs(tab.id)
        })
        add(MenuEntry.Item(s.tabCloseRight) {
            half.closeRightTabs(tab.id)
        })

        add(MenuEntry.Divider)

        add(MenuEntry.Item(s.tabSplitOther) {
            oppositeHalf.activateTab(tab.id)
        })

        add(MenuEntry.Item(s.tabSplitMoveOther) {
            half.closeTab(tab.id)
            oppositeHalf.activateTab(tab.id)
            oppositeHalf.inFocus = true
        })

        if(tab is TabCashFileText) {
            add(MenuEntry.Divider)

            add(MenuEntry.Item(s.tabShowInTree) {
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