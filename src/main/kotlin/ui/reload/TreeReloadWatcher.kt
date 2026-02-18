package non.era.aisdme.ui.reload

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.FileOpened
import non.era.aisdme.utils.Log
import non.era.aisdme.bus.ProjectOpen
import non.era.aisdme.bus.RebuilDirRiff
import non.era.aisdme.ui.UiController
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds

class TreeReloadWatcher(
    private val bus: AppEventBus
) : UiController() {
    //todo mutex in service
    private val watchService = FileSystems.getDefault().newWatchService()
    private val pendingDirs = mutableSetOf<File>()
    private val pandingMutex = Mutex()
    private val appliPandingMutex = Mutex()

    init{
        watchLoop()
        scope.launch {
            bus.events.collect { event ->
                when (event) {
                    is FileOpened -> {}
                    is RebuilDirRiff -> {}
                    is ProjectOpen -> {
                        scope.launch {
                            registerRecursively(event.root.toPath())
                        }
                    }
                }
            }
        }
    }

    fun watchLoop() = scope.launch {
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

                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(fullPath)) { //новая директория
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

    //todo смена проекта, нужно ли забывать
    private fun registerRecursively(root: Path) {
        Log.d("registerRecursively")
        fun registerDir(path: Path) =
            path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )

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
                bus.publish(RebuilDirRiff(dir))
            }
        } finally {
            appliPandingMutex.unlock()
        }
    }
}