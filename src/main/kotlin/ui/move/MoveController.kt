package non.era.aisdme.ui.move

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import non.era.aisdme.ui.UiController
import non.era.aisdme.ui.project.data.ProjectNode
import non.era.aisdme.utils.Log
import java.io.File

class MoveController : UiController() {
    private var _draggingNode by mutableStateOf(false)
    private var _dragPosition by mutableStateOf(Offset.Zero)
    private val _nodeBounds = mutableStateMapOf<ProjectNode, Rect>()
    private var _selectedNodeOnStart: List<ProjectNode>? = null

    private var _listBounds: Rect? = null
    private var _autoScrollJob: Job? = null
    private var _scrollState: LazyListState? = null

    fun getDragPosition(): Offset? = if(_draggingNode && _selectedNodeOnStart != null) _dragPosition else null
    fun getNodesDragable() = _selectedNodeOnStart

    fun setListBounds(listBounds: Rect) {
        _listBounds = listBounds
    }

    fun setScrollState(newScrollState: LazyListState) {
        _scrollState = newScrollState
    }

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
        checkAutoScroll()
    }

    fun cancelDrag() {
        Log.d("cancel drop")
        _draggingNode = false
        _autoScrollJob?.cancel()
    }

    fun forceFinish() {
        if(!_draggingNode) return
        Log.d("force finish drop")
        _draggingNode = false
        _autoScrollJob?.cancel()
    }

    fun isDropTarget(node: ProjectNode) =
        if(_draggingNode) {
            _nodeBounds[node]?.contains(_dragPosition) == true
        } else {
            false
        }

    fun finishDrop(
        resultDropOnTree: (src: List<ProjectNode>, file: File) -> Unit,
        resultDropOutsideTree: (src: List<ProjectNode>, position: Offset) -> Unit
    ) {
        Log.d("finish drop")
        if(!_draggingNode) return
        _draggingNode = false
        _autoScrollJob?.cancel()
        if(_selectedNodeOnStart == null) return


        val target = _nodeBounds.entries.firstOrNull { it.value.contains(_dragPosition) }?.key ?: run {
            resultDropOutsideTree(_selectedNodeOnStart!!, _dragPosition)
            return@finishDrop
        }

        _selectedNodeOnStart!!.forEach { node ->
            if(node.getFile() == target.getFile()) return@finishDrop
        }

        val tergetFile = if(target.getFile().isDirectory) {
            target.getFile()
        } else {
            target.getFile().parentFile
        }

        resultDropOnTree(_selectedNodeOnStart!!, tergetFile)
    }

    private fun checkAutoScroll() {
        val bounds = _listBounds ?: return
        val pos = _dragPosition

        val edge = 50f // зона активации

        val scrollDir = when {
            pos.y < bounds.top + edge -> -1
            pos.y > bounds.bottom - edge -> 1
            else -> 0
        }

        if (scrollDir == 0) {
            _autoScrollJob?.cancel()
            return
        }

        if (_autoScrollJob?.isActive == true) return

        _autoScrollJob = scope.launch {
            autoScrollLoop(scrollDir)
        }
    }

    private suspend fun autoScrollLoop(dir: Int) {
        val state = _scrollState ?: return

        while (isActive) {
            withContext(Dispatchers.Swing) {
                state.scrollBy(dir * 5f) // скорость
            }
            delay(16) // ~60 FPS
        }
    }
}