package non.era.aisdme.ui.project

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.IControler
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*

class ProjectController(
    projectRoot: File
) : TreeState, IControler {
    
    val root = projectRoot
    val watchService = FileSystems.getDefault().newWatchService()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingDirs = mutableSetOf<File>()
    private val pandingMutex = Mutex()
    private val appliPandingMutex = Mutex()

    private val projectTree = ProjectTreeController()

    init {
        scope.launch {
            projectTree.rebuildAllTree(root)
        }
        registerRecursively(root.toPath())
        watchLoop()
    }

    private fun registerRecursively(root: Path) {
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
                    pendingDirs += parentDir
                }
                scope.launch {
                    applyBatch()
                }

                if (event.kind() == ENTRY_CREATE && Files.isDirectory(fullPath)) { //новая директория
                    registerRecursively(fullPath)
                }
            }

            if (!key.reset()) break
        }
    }

    suspend fun applyBatch() {
        if (!appliPandingMutex.tryLock()) return

        try {
            delay(300)
            val dirs = pandingMutex.withLock {
                val dirs = pendingDirs.toList()
                pendingDirs.clear()
                return@withLock dirs
            }

            for (dir in dirs) {
                projectTree.rebuildDir(dir)
            }
        } finally {
            appliPandingMutex.unlock()
        }
    }

    override fun getNodes() = projectTree.getNodes()

    override fun dispose() {
        scope.cancel()
        projectTree.dispose()
        watchService.close()
    }
}