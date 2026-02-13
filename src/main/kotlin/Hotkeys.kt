package non.era.aisdme

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key

enum class Hotkeys (
    val vis: String,
    val ifDu: (event: KeyEvent) -> Boolean
){
    COPY("Ctrl + c", {it.isCtrlPressed && it.key == Key.C}),
    PASTE("Ctrl + v", {it.isCtrlPressed && it.key == Key.V}),
    CUT("Ctrl + x", {it.isCtrlPressed && it.key == Key.X}),
    OPEN_DOLPHIN("Alt + e", {it.isAltPressed && it.key == Key.E}),
    DELETE("Del", {it.key == Key.Delete}),
    ENTER("Enter", {it.key == Key.Enter || it.key == Key.NumPadEnter}),
    ESC("esc", {it.key == Key.Escape}),
    TOP("up", {it.key == Key.DirectionUp}),
    BOTTOM("down", {it.key == Key.DirectionDown}),
    LEFT("left", {it.key == Key.DirectionLeft}),
    RIGHT("right", {it.key == Key.DirectionRight}),
}