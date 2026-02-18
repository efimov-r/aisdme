package non.era.aisdme.ui.move

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import non.era.aisdme.utils.Log
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.ProjectNode

class TreeDragState {
    var draggingNode by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
}

class MoveController : UiController() {
    private val _dragState = TreeDragState()
    private val _nodeBounds = mutableStateMapOf<ProjectNode, Rect>()

    //todo не гетить мутейблы бля
    fun getDragState() = _dragState
    fun getNodeBounds() = _nodeBounds

    fun finishDrop() {
        if(!_dragState.draggingNode) return
        val target = _nodeBounds.entries.firstOrNull { it.value.contains(_dragState.dragPosition) }?.key ?: return
        Log.d("move to: ${target.getFile()}")
        _dragState.draggingNode = false
    }

    private fun canMove(src: ProjectNode, dst: ProjectNode): Boolean {
//        if (!dst.isDirectory) return false
//        if (dst == src) return false
//        if (dst.isChildOf(src)) return false
        return true
    }

}