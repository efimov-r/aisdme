package non.era.aisdme.ui.reload

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    private val watchKinds = arrayOf<WatchEvent.Kind<*>>( //без ENTRY_MODIFY: запись в файл не меняет дерево
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
    )
    private val keysMutex = Mutex()
    private val keys = mutableSetOf<WatchKey>()

    private val pendingDirs = mutableSetOf<File>()
    private val pendingMutex = Mutex()
    private val pendingSignal = Channel<Unit>(Channel.CONFLATED) //сигнал, пришедший во время пакета, не теряется

    private val commands = Channel<suspend () -> Unit>(Channel.UNLIMITED) //смена проекта и регистрации — по порядку
    private var root: Path? = null //меняется только в commandLoop

    init {
        watchLoop()
        applyLoop()
        commandLoop()
    }

    private fun commandLoop() = scope.launch {
        for (command in commands) command()
    }

    fun dirsLoaded(dirs: List<File>) { //Linux: следим только за прочитанными в дерево папками, не за всем проектом
        if (Platform.current.watchSubtree) return
        commands.trySend { dirs.forEach { registerInRoot(it.toPath()) } }
    }

    private suspend fun registerInRoot(dir: Path) {
        val r = root ?: return
        if (dir.startsWith(r)) register(dir) //папки старого проекта не регистрируются
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

                pendingSignal.trySend(Unit) //новая папка регистрируется, когда её прочитают в дерево
            }

            if (!key.reset()) {
                key.cancel()
                keysMutex.withLock {
                    keys.remove(key)
                }
            }
        }
    }

    fun switchProject(newRoot: Path) {
        commands.trySend { //в очередь сразу: раньше, чем придут прочитанные папки нового проекта
            root = newRoot
            keysMutex.withLock {
                keys.forEach { it.cancel() }// отменяем старые
                keys.clear()
            }

            pendingMutex.withLock { pendingDirs.clear() }

            register(newRoot) //Windows — всё поддерево, Linux — только корень, остальное в dirsLoaded
        }
    }

    private suspend fun register(dir: Path) {
        runCatching {
            val key = Platform.current.registerWatch(dir, watchService, watchKinds)
            keysMutex.withLock { keys += key }
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