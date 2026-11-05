package non.era.aisdme.ui.reload

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import non.era.aisdme.bus.AppEventBus
import non.era.aisdme.bus.RebuilDirRiff
import non.era.aisdme.platform.Platform
import non.era.aisdme.ui.UiController
import java.io.File
import java.nio.file.*

class TreeReloadWatcher(
    private val bus: AppEventBus
) : UiController() {

    private val watchService = FileSystems.getDefault().newWatchService()
    private val watchKinds = arrayOf<WatchEvent.Kind<*>>(
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY,
    )
    private val keysMutex = Mutex()
    private val keys = mutableSetOf<WatchKey>()

    private val pendingDirs = mutableSetOf<File>()
    private val pendingMutex = Mutex()
    private val pendingSignal = Channel<Unit>(Channel.CONFLATED) //сигнал, пришедший во время пакета, не теряется

    init {
        watchLoop()
        applyLoop()
    }

    private fun watchLoop() = scope.launch {
        while (isActive) {
            val key = runCatching { watchService.take() }.getOrNull() ?: break

            for (event in key.pollEvents()) {
                val dir = key.watchable() as Path
                val fullPath = (event.context() as? Path)?.let { dir.resolve(it) }

                val changedDir = fullPath?.parent?.toFile() ?: dir.toFile() //OVERFLOW без пути — перечитать всю папку ключа

                pendingMutex.withLock {
                    pendingDirs += changedDir
                }

                pendingSignal.trySend(Unit)

                if (
                    fullPath != null &&
                    !Platform.current.watchSubtree &&
                    event.kind() == StandardWatchEventKinds.ENTRY_CREATE &&
                    Files.isDirectory(fullPath)
                ) {
                    registerRecursively(fullPath)
                }
            }

            if (!key.reset()) {
                key.cancel()
                keysMutex.withLock {
                    keys.remove(key)
                }
            }
        }
    }

    fun switchProject(root: Path) {
        scope.launch {
            keysMutex.withLock {
                keys.forEach { it.cancel() }// отменяем старые
                keys.clear()
            }

            pendingMutex.withLock { pendingDirs.clear() }

            if (Platform.current.watchSubtree) register(root) else registerRecursively(root)
        }
    }

    private suspend fun register(dir: Path) {
        runCatching {
            val key = Platform.current.registerWatch(dir, watchService, watchKinds)
            keysMutex.withLock { keys += key }
        }
    }

    private suspend fun registerRecursively(root: Path) = coroutineScope {
        Files.walk(root).use { stream ->
            val dirs = stream.filter { Files.isDirectory(it) && !Files.isSymbolicLink(it) }
                .toList() // materialize list
            dirs.map { dir ->
                launch { register(dir) }
            }.joinAll()
        }
    }

    private fun applyLoop() = scope.launch {
        while (isActive) {
            pendingSignal.receive()
            delay(200) //собрать пачку событий

            val dirs = pendingMutex.withLock {
                val list = pendingDirs.toList()
                pendingDirs.clear()
                list
            }

            dirs.forEach {
                if(it.exists()) {
                    bus.publish(RebuilDirRiff(it))
                }
            }
        }
    }

    override fun disposeAfter() {
        runBlocking {
            keysMutex.withLock {
                keys.forEach { it.cancel() }
                keys.clear()
            }
        }
        watchService.close()
    }
}