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

class MoveController : UiController() {
    private var _draggingNode by mutableStateOf(false)
    private var _dragPosition by mutableStateOf(Offset.Zero)
    private val _nodeBounds = mutableStateMapOf<ProjectNode, Rect>()

    fun disposeElement(node: ProjectNode) {
        _nodeBounds.remove(node)
    }

    fun setNodeBounds(node: ProjectNode, rect: Rect) {
        _nodeBounds[node] = rect
    }

    fun onDragStart(offset: Offset) {
        _draggingNode = true
        _dragPosition = offset
    }

    fun setDragPosition(position: Offset) {
        _dragPosition = position
    }

    fun cancelDrag() {
        _draggingNode = false
    }

    fun isDropTarget(node: ProjectNode) =
        if(_draggingNode) {
            _nodeBounds[node]?.contains(_dragPosition) == true
        } else {
            false
        }

    fun finishDrop() {
        if(!_draggingNode) return
        val target = _nodeBounds.entries.firstOrNull { it.value.contains(_dragPosition) }?.key ?: return
        Log.d("move to: ${target.getFile()}")
        _draggingNode = false
    }

    private fun canMove(src: ProjectNode, dst: ProjectNode): Boolean {
//        if (!dst.isDirectory) return false
//        if (dst == src) return false
//        if (dst.isChildOf(src)) return false
        return true
    }

}