package non.era.aisdme.ui.components.custom.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.ui.components.contexmenu.ButtonsMenu
import non.era.aisdme.ui.components.contexmenu.MenuEntry
import non.era.aisdme.ui.components.contexmenu.SmartMenuPositionProvider
import non.era.aisdme.ui.editor.HalfEditorTabs
import non.era.aisdme.utils.ClipboardHelper

@Composable
fun TabMenuView(
    position: Offset,
    half: HalfEditorTabs,
    onDismiss: () -> Unit
) {
    val editController = LocalAppController.current.editorController
    buildList {
        if(ClipboardHelper.clipboardHasText()) {
            add(MenuEntry.Item("New from clipboard", "paste") {
                val tabId = editController.addFromCpipboardTab()
                half.activateTab(tabId)
            })
        } else {
            add(MenuEntry.Divider)
        }
    }.let {
        ButtonsMenu(
            entries = it,
            popupPositionProvider = SmartMenuPositionProvider(position),
            onDismiss = onDismiss
        )
    }
}