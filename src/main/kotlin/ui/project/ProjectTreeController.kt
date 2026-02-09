package non.era.aisdme.ui.project

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext
import non.era.aisdme.IControler
import non.era.aisdme.Log
import java.io.File
import java.nio.file.Files

class ProjectTreeController: IControler, TreeState {
    private val nodes_ = mutableStateListOf<ProjectNode>()
    private val indexDir: MutableMap<File, DirNode> = mutableMapOf()

    override fun getNodes() = nodes_

    suspend fun rebuildAllTree(root: File) {
        val newTree = buildProjectTree(root)
        withContext(Dispatchers.Swing) {
            nodes_.clear()
            nodes_.addAll(newTree)
        }
    }

    suspend fun rebuildDir(deer: File): Boolean =
        withContext(Dispatchers.Swing) {
            val dn = findDirNodeFast(deer) ?: run {
                Log.d("no fast find dir")
                findDirNode(nodes_, deer)
            }

            dn?.let { dirNode->
                Log.i("rebuild child: ${dirNode.file}")
                rebuildDirDiff(dirNode)
                return@withContext true
            }
            Log.e("findDirNode is null, reload all")
            return@withContext false
        }

    private fun rebuildDirDiff(node: DirNode) {
        val old = node.children.associateBy { it.file.path }
        val fresh = buildProjectTree(node.file, node.depth + 1)

        val result = mutableListOf<ProjectNode>()

        for (newNode in fresh) {
            val oldNode = old[newNode.file.path]
            if (oldNode is DirNode && newNode is DirNode) {
                // сохраняем старый узел, обновляем детей
                rebuildDirDiff(oldNode)
                result += oldNode
            } else {
                result += newNode
                Log.d("new node ${newNode.file}")
            }
        }

        node.children.clear()
        node.children.addAll(result)
    }

    private fun findDirNodeFast(dir: File): DirNode? = indexDir[dir]


    private fun findDirNode(
        nodes: List<ProjectNode>,
        dir: File
    ): DirNode? {
        for (node in nodes) {
            if (node is DirNode) {
                if (node.file == dir) return node
                findDirNode(node.children, dir)?.let { return it }
            }
        }
        return null
    }


    private fun buildProjectTree(
        root: File,
        depth: Int = 0
    ): List<ProjectNode> {
        if (!root.exists()) return emptyList()

        return root.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?.map { f ->
                if (f.isDirectory) {
                    val a = DirNode(
                        file = f,
                        depth = depth,
                        children = buildProjectTree(f, depth + 1)
                    )
                    indexDir[f] = a
                    a
                } else if(Files.isSymbolicLink(f.toPath())) {
                    LinkNode(
                        file = f,
                        depth = depth
                    )
                } else {
                    FileNode(
                        file = f,
                        depth = depth
                    )
                }
            } ?: emptyList()
    }

    override fun dispose() {
        //ignore
    }
}