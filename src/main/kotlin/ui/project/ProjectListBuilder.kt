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

    fun build(nodes: List<ProjectNode>) : ProjectListBuilder {
        _result = mutableListOf()
        _selected = listOf()
        nodes.forEach { node ->
            nodeBuild(node)
        }
        return this
    }

    fun buildSelected(selected: List<ProjectNode>) : ProjectListBuilder {
        _selected = selected
        return this
    }

    fun nextOf(node: ProjectNode?) : ProjectNode?  {
        if(node == null) return null
        val i = _result.indexOfFirst { it.equalsOf(node) }
        return _result.getOrNull(i + 1) ?: node
    }

    fun lastOf(node: ProjectNode?) : ProjectNode?  {
        if(node == null) return null
        val i = _result.indexOfFirst { it.equalsOf(node) }
        return _result.getOrNull(i - 1) ?: node
    }

    fun fromAndTo(to: ProjectNode?): List<ProjectNode> {
        if(to == null) return _selected

        val indexToObj = _result.withIndex().firstOrNull { it.value.equalsOf(to) }?.index ?: return listOf(to)

        val l = _selected.mapNotNull { sn-> //лист позиций селекнутых элементов
            _result.forEachIndexed { index, node ->
                if(node.equalsOf(sn)) {
                    return@mapNotNull index
                }
            }
            return@mapNotNull null
        }

        val maxSelected = l.maxOrNull() ?: return _selected
        val minSelected = l.minOrNull() ?: return _selected

        val max = max(maxSelected, indexToObj)
        val min = min(minSelected, indexToObj)

        return _result.slice(min..max)
    }
}