package non.era.aisdme.ui.move

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.ProjectNode
import java.io.File

class MoveController : UiController() {
    private var _draggingNode by mutableStateOf(false)
    private var _dragPosition by mutableStateOf(Offset.Zero)
    private val _nodeBounds = mutableStateMapOf<ProjectNode, Rect>()
    private var _selectedNodeOnStart: List<ProjectNode>? = null

    fun disposeElement(node: ProjectNode) {
        _nodeBounds.remove(node)
    }

    fun setNodeBounds(node: ProjectNode, rect: Rect) {
        _nodeBounds[node] = rect
    }

    fun onDragStart(offset: Offset, selectedNode: List<ProjectNode>) {
        _draggingNode = true
        _dragPosition = offset
        _selectedNodeOnStart = selectedNode
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

    fun finishDrop(resultDrop: (src: List<ProjectNode>, file: File) -> Unit) {
        if(!_draggingNode) return
        _draggingNode = false
        val target = _nodeBounds.entries.firstOrNull { it.value.contains(_dragPosition) }?.key ?: return

        if(!target.getFile().isDirectory) return
        if(_selectedNodeOnStart == null) return
        _selectedNodeOnStart!!.forEach { node ->
            if(node.getFile() == target.getFile()) return@finishDrop
        }

        resultDrop(_selectedNodeOnStart!!, target.getFile())
    }
}