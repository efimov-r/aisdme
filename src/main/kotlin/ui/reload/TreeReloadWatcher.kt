package non.era.aisdme.ui.reload

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.RebuilDirRiff
import non.era.aisdme.ui.UiController
import java.io.File
import java.nio.file.*

class TreeReloadWatcher(
    private val bus: AppEventBus
) : UiController() {

    private val watchService = FileSystems.getDefault().newWatchService()
    private val keys = mutableSetOf<WatchKey>()

    private val pendingDirs = mutableSetOf<File>()
    private val pendingMutex = Mutex()

    private val applyMutex = Mutex()

    init {
        watchLoop()
    }

    private fun watchLoop() = scope.launch {
        while (isActive) {
            val key = runCatching { watchService.take() }.getOrNull() ?: break

            for (event in key.pollEvents()) {
                val dir = key.watchable() as Path
                val fullPath = dir.resolve(event.context() as Path)

                val parentDir = fullPath.parent?.toFile() ?: continue

                pendingMutex.withLock {
                    pendingDirs += parentDir
                }

                launch { applyBatch() }

                if (
                    event.kind() == StandardWatchEventKinds.ENTRY_CREATE &&
                    Files.isDirectory(fullPath)
                ) {
                    registerRecursively(fullPath)
                }
            }

            if (!key.reset()) {
                key.cancel()
                keys.remove(key)
            }
        }
    }

    fun switchProject(root: Path) {
        scope.launch {
            keys.forEach { it.cancel() }// отменяем старые
            keys.clear()

            pendingMutex.withLock { pendingDirs.clear() }

            registerRecursively(root)
        }
    }

    private fun registerRecursively(root: Path) {
        fun registerDir(path: Path) {
            try {
                val key = path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )
                keys += key
            } catch (_: Exception) {
                // директория могла исчезнуть — игнор
            }
        }

        Files.walk(root).use { stream ->
            stream
                .filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }
                .forEach { registerDir(it) }
        }
    }

    private suspend fun applyBatch() {
        if (!applyMutex.tryLock()) return

        try {
            delay(200)

            val dirs = pendingMutex.withLock {
                val list = pendingDirs.toList()
                pendingDirs.clear()
                list
            }

            dirs.forEach { bus.publish(RebuilDirRiff(it)) }

        } finally {
            applyMutex.unlock()
        }
    }

    override fun disposeAfter() {
        keys.forEach { it.cancel() }
        keys.clear()
        watchService.close()
    }
}