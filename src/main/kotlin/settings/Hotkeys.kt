package non.era.aisdme.settings

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key

enum class Hotkeys (
    val vis: String,
    val ifDu: (event: KeyEvent) -> Boolean
){
    COPY("Ctrl + c", {it.isCtrlPressed && it.key == Key.Companion.C}),
    PASTE("Ctrl + v", {it.isCtrlPressed && it.key == Key.Companion.V}),
    CUT("Ctrl + x", {it.isCtrlPressed && it.key == Key.Companion.X}),
    SHOW_IN_FILE_MANAGER("Alt + e", {it.isAltPressed && it.key == Key.Companion.E}),
    DELETE("Del", {it.key == Key.Companion.Delete}),
    RENAME("Shift + F6", {it.isShiftPressed && it.key == Key.Companion.F6}),
    ENTER("Enter", {it.key == Key.Companion.Enter || it.key == Key.Companion.NumPadEnter}),
    ESC("esc", {it.key == Key.Companion.Escape}),
    TOP("up", {it.key == Key.Companion.DirectionUp}),
    BOTTOM("down", {it.key == Key.Companion.DirectionDown}),
    SHIFT_TOP("Shift + up", {it.isShiftPressed && it.key == Key.Companion.DirectionUp}),
    SHIFT_BOTTOM("Shift + down", {it.isShiftPressed && it.key == Key.Companion.DirectionDown}),
    LEFT("left", {it.key == Key.Companion.DirectionLeft}),
    RIGHT("right", {it.key == Key.Companion.DirectionRight}),
}