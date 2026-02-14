package non.era.aisdme.ui.project

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.Log
import non.era.aisdme.ui.UiController
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*

class ProjectController(
    projectRoot: File
) : TreeState, UiController() {
    
    val root = projectRoot
    val watchService = FileSystems.getDefault().newWatchService()
    private val pendingDirs = mutableSetOf<File>()
    private val pandingMutex = Mutex()
    private val appliPandingMutex = Mutex()

    private val projectTree = ProjectTreeController()

    init {
        scope.launch {
            projectTree.setRootProject(root)
            projectTree.rebuildAllTree()
        }
        registerRecursively(root.toPath())
        watchLoop()
        projectTree.subscriptionError { emitGlobalMessage(it) }
    }

    fun cutNoteMarkRecursive(node: ProjectNode) = projectTree.cutNoteMarkRecursive(node)

    private fun registerRecursively(root: Path) {
        Log.d("registerRecursively")
        fun registerDir(path: Path) =
            path.register(watchService,ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)

        Files.walk(root)
            .filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }
            .forEach { registerDir(it) }
    }

    private fun watchLoop() = scope.launch {
        while (isActive) {
            val key = runCatching { watchService.take() }.getOrNull() ?: break

            for (event in key.pollEvents()) {
                val fullPath = (key.watchable() as Path).resolve(event.context() as Path)
                val parentDir = fullPath.parent?.toFile() ?: continue
                pandingMutex.withLock {
                    Log.d("new pendingDir ${parentDir.toString()}")
                    pendingDirs += parentDir
                }
                scope.launch {
                    applyBatch()
                }

                if (event.kind() == ENTRY_CREATE && Files.isDirectory(fullPath)) { //новая директория
                    Log.d("new dir register recursively")
                    registerRecursively(fullPath)
                }
            }

            if (!key.reset()) {
                key.cancel()
                continue
            }
        }
    }

    suspend fun applyBatch() {
        if (!appliPandingMutex.tryLock()) return

        try {
            delay(200)
            val dirs = pandingMutex.withLock {
                val dirs = pendingDirs.toList()
                pendingDirs.clear()
                return@withLock dirs
            }

            for (dir in dirs) {
                projectTree.rebuildDirDiff(dir)
            }
        } finally {
            appliPandingMutex.unlock()
        }
    }

    fun reloadDeerFromDisk() = projectTree.reloadDirFromDisk()

    override fun getNodes() = projectTree.getNodes()
    override fun getSelectedNodes(): List<ProjectNode> = projectTree.getSelectedNodes()
    override fun reselectNode(node: ProjectNode) = projectTree.reselectNode(node)
    override fun addSelectNode(node: ProjectNode) = projectTree.addSelectNode(node)

    override fun disposeAfter() {
        projectTree.dispose()
        watchService.close()
    }
}