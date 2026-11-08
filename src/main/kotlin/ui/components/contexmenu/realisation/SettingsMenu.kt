package non.era.aisdme.ui.components.contexmenu.realisation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import non.era.aisdme.debug.debugTag
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.i18n.I18n
import non.era.aisdme.i18n.Lang
import non.era.aisdme.ui.components.contexmenu.ContextMenu
import non.era.aisdme.ui.components.contexmenu.ContextMenuState
import non.era.aisdme.ui.components.contexmenu.MenuEntry
import non.era.aisdme.ui.components.over.SvgButton

@Composable
fun SettingsButton(modifier: Modifier = Modifier) {
    val menuState = remember { ContextMenuState() }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(modifier.onGloballyPositioned { coords = it }) {
        SvgButton("settings", I18n.s.settings, Modifier.debugTag("settings"), size = 32.dp) {
            coords?.boundsInWindow()?.let { menuState.show(Offset(it.right, it.bottom)) } //меню раскрывается влево-вниз
        }

        if (menuState.isVisible) {
            SettingsMenu(menuState) { menuState.hide() }
        }
    }
}

@Composable
private fun SettingsMenu(state: ContextMenuState, onDismiss: () -> Unit) {
    val app = LocalAppController.current
    val s = I18n.s
    val dark = app.themeController.isDarkTheme

    buildList {
        add(MenuEntry.Header(s.theme))
        add(MenuEntry.Item(s.themeLight, checked = !dark) { app.themeController.select(false) })
        add(MenuEntry.Item(s.themeDark, checked = dark) { app.themeController.select(true) })

        add(MenuEntry.Divider)

        add(MenuEntry.Header(s.language))
        Lang.entries.forEach { lang ->
            add(MenuEntry.Item(lang.title, checked = lang == app.languageController.lang) {
                app.languageController.select(lang)
            })
        }
    }.let {
        ContextMenu(
            entries = it,
            state = state,
            onDismiss = onDismiss,
            width = 200.dp,
        )
    }
}
