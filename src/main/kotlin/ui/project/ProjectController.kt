package non.era.aisdme.ui.project

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.IControler
import non.era.aisdme.Log
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*

class ProjectController(
    projectRoot: File
) : ProjectState, IControler {
    
    val root = projectRoot
    val watchService = FileSystems.getDefault().newWatchService()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reloadJob: Job? = null

    private val nodes_ = mutableStateListOf<ProjectNode>()

    init {
        nodes_.addAll(buildProjectTree(root))
        registerRecursively(root.toPath())
        watchLoop()
    }

    private fun registerRecursively(root: Path) {
        Files.walk(root)
            .filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }
            .forEach { registerDir(it) }
    }

    private fun registerDir(path: Path) {
        path.register(
            watchService,
            ENTRY_CREATE,
            ENTRY_DELETE,
            ENTRY_MODIFY
        )
    }

    private fun watchLoop() = scope.launch {
        while (isActive) {
            val key = runCatching { watchService.take() }.getOrNull() ?: break

            for (event in key.pollEvents()) {
                val dir = key.watchable() as Path
                val fullPath = dir.resolve(event.context() as Path)

                val parentDir = fullPath.parent?.toFile() ?: continue

                withContext(Dispatchers.Swing) {
                    val dirNode = findDirNode(nodes_, parentDir)
                    if (dirNode != null) {
                        val newChildren = buildProjectTree(dirNode.file, dirNode.depth + 1)
                        Log.i("rebuild child: ${dirNode.file}")
                        dirNode.children.clear()
                        dirNode.children.addAll(newChildren)
                    } else {
                        scheduleReload()
                        Log.i("findDirNode is null, reload all")
                    }
                }

                // если создана новая директория — регистрируем её
                if (event.kind() == ENTRY_CREATE && Files.isDirectory(fullPath)) {
                    registerRecursively(fullPath)
                }
            }

            if (!key.reset()) break
        }
    }

    private fun scheduleReload() {
        reloadJob?.cancel()
        reloadJob = scope.launch {
            delay(400)
            yield()
            val newTree = buildProjectTree(root)
            withContext(Dispatchers.Swing) {
                nodes_.clear()
                nodes_.addAll(newTree)
            }
        }
    }

    override fun getNodes() = nodes_

    override fun dispose() {
        scope.cancel()
        watchService.close()
    }

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
                    DirNode(
                        file = f,
                        depth = depth,
                        children = buildProjectTree(f, depth + 1)
                    )
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
}