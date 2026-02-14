package non.era.aisdme.ui.components.contexmenu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

class ContextMenuState {
    var isVisible by mutableStateOf(false)
    var position by mutableStateOf(Offset.Zero)

    fun show(pos: Offset) {
        position = pos
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}
