package non.era.aisdme.ui.project

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.Log
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY

class TreeReloadWatcher(
    val rebuildDir: suspend (dir: File) -> Unit
) {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val pendingDirs = mutableSetOf<File>()
    private val pandingMutex = Mutex()
    private val appliPandingMutex = Mutex()

    fun watchLoop(scope: CoroutineScope) = scope.launch {
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

    fun registerRecursively(root: Path) {
        Log.d("registerRecursively")
        fun registerDir(path: Path) =
            path.register(watchService,ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)

        Files.walk(root)
            .filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }
            .forEach { registerDir(it) }
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
                rebuildDir(dir)
            }
        } finally {
            appliPandingMutex.unlock()
        }
    }
}