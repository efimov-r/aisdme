package non.era.aisdme.ui.project

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Rect
import non.era.aisdme.Log
import non.era.aisdme.ui.UiController

class MoveController : UiController() {
    private val _dragState = TreeDragState()
    private val _nodeBounds = mutableStateMapOf<ProjectNode, Rect>()

    fun getDragState() = _dragState
    fun getNodeBounds() = _nodeBounds

    fun finishDrop() {
        val dragged = _dragState.draggingNode ?: return
        val target = _nodeBounds.entries.firstOrNull { it.value.contains(_dragState.dragPosition) }?.key ?: return
        moveNode(dragged, target)
        _dragState.draggingNode = null
    }

    private fun canMove(src: ProjectNode, dst: ProjectNode): Boolean {
//        if (!dst.isDirectory) return false
//        if (dst == src) return false
//        if (dst.isChildOf(src)) return false
        return true
    }

    private fun moveNode(dragged: ProjectNode, target: ProjectNode){
        Log.d("move: ${dragged.getFile()} to ${target.getFile()}")
        //todo
    }
}