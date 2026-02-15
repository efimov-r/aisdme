package non.era.aisdme.ui.project

import kotlin.math.max
import kotlin.math.min

object ProjectListBuilder {
    private var _result: MutableList<ProjectNode> = mutableListOf()
    private var _selected: List<ProjectNode> = listOf()

    private fun dirBuild(node: DirNode) {
        _result.add(node)
        if (node.expanded.value) {
            node.children.forEach { child ->
                nodeBuild(child)
            }
        }
    }

    private fun nodeBuild(node: ProjectNode) {
        when (node) {
            is DirNode -> dirBuild(node)
            is FileNode -> _result.add(node)
            is LinkNode -> _result.add(node)
        }
    }

    fun build(nodes: List<ProjectNode>, selected: List<ProjectNode>) : ProjectListBuilder {
        _result = mutableListOf()
        _selected = selected
        nodes.forEach { node ->
            nodeBuild(node)
        }
        return this
    }

    private fun getNextNodeOf(node: ProjectNode) : ProjectNode {
        val i = _result.indexOfFirst { it.equalsOf(node) }
        return _result.getOrNull(i + 1) ?: node
    }

    fun nextOf(node: ProjectNode) {
        val r = getNextNodeOf(node)
        _selected.forEach { it.setSelected(false) }
        r.setSelected(true)
    }

    private fun getLastNodeOf(node: ProjectNode) : ProjectNode {
        val i = _result.indexOfFirst { it.equalsOf(node) }
        return _result.getOrNull(i - 1) ?: node
    }

    fun lastOf(node: ProjectNode) {
        val r = getLastNodeOf(node)
        _selected.forEach { it.setSelected(false) }
        r.setSelected(true)
    }

    private fun listPositionSelectedNodes(): List<Int> =
        _selected.mapNotNull { sn->
            _result.forEachIndexed { index, node ->
                if(node.equalsOf(sn)) {
                    return@mapNotNull index
                }
            }
            return@mapNotNull null
        }

    fun fromAndTo(to: ProjectNode) {
        val indexToObj = _result.withIndex().firstOrNull { it.value.equalsOf(to) }?.index ?: return

        val l = listPositionSelectedNodes()

        val maxSelected = l.maxOrNull() ?: run{
            to.setSelected(true)
            return
        }
        val minSelected = l.minOrNull() ?: run{
            to.setSelected(true)
            return
        }

        val max = max(maxSelected, indexToObj)
        val min = min(minSelected, indexToObj)

        _result.slice(min..max).forEach {
            it.setSelected(true)
        }
    }

    fun shiftTop(firstMovement: Movement?) {
        if(_selected.isEmpty()) return

        if(_selected.size == 1) {
            getLastNodeOf(_selected[0]).setSelected(true)
        } else {
            if(firstMovement == null) return

            when(firstMovement){
                Movement.TOP -> {
                    val minSelected = listPositionSelectedNodes().minOrNull() ?: return
                    getLastNodeOf(_result[minSelected]).setSelected(true)
                }
                Movement.BOTTOM -> {
                    val maxSelected = listPositionSelectedNodes().maxOrNull() ?: return
                    _result[maxSelected].setSelected(false)
                }
            }
        }
    }

    fun shiftBottom(firstMovement: Movement?) {
        if(_selected.isEmpty()) return

        if(_selected.size == 1) {
            getNextNodeOf(_selected[0]).setSelected(true)
        } else {
            if(firstMovement == null) return

            when(firstMovement){
                Movement.TOP -> {
                    val minSelected = listPositionSelectedNodes().minOrNull() ?: return
                    _result[minSelected].setSelected(false)
                }
                Movement.BOTTOM -> {
                    val maxSelected = listPositionSelectedNodes().maxOrNull() ?: return
                    getNextNodeOf(_result[maxSelected]).setSelected(true)
                }
            }
        }
    }
}