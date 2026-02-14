package non.era.aisdme.ui.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

class TreeDragState {
    var draggingNode by mutableStateOf<ProjectNode?>(null)
    var dragPosition by mutableStateOf(Offset.Zero)
}