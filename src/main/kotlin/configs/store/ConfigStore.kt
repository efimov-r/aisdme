package non.era.aisdme.configs.store

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class ConfigStore<T>(
    private val file: Path,
    private val serializer: KSerializer<T>,
    private val default: () -> T,
) {
    private val mutex = Mutex()
    var autosaveJob: Job? = null

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private var cache: T? = null

    init {
        ensureExists()
    }

    suspend fun get(): T = mutex.withLock {
        cache ?: loadFromDisk().also { cache = it }
    }

    suspend fun reload(): T = mutex.withLock {
        loadFromDisk().also { cache = it }
    }

    suspend fun update(block: (T) -> T): T =
        mutex.withLock {
            val current = cache ?: loadFromDisk()
            val updated = block(current)

            atomicWrite(updated)

            cache = updated
            updated
        }


//    fun updateDeffered(block: (T) -> T) {
//        autosaveJob?.cancel()
//        autosaveJob = scope.launch {
//            delay(500)
//            mutex.withLock {
//                val current = cache ?: loadFromDisk()
//                val updated = block(current)
//
//                atomicWrite(updated)
//
//                cache = updated
//                updated
//            }
//        }
//    }

    private fun ensureExists() {
        Files.createDirectories(file.parent)

        if (!Files.exists(file)) {
            atomicWrite(default())
        }
    }

    private fun loadFromDisk(): T {
        val text = Files.readString(file)
        return json.decodeFromString(serializer, text)
    }

    private fun atomicWrite(data: T) {
        val tmp = file.resolveSibling(file.fileName.toString() + ".tmp")

        val text = json.encodeToString(serializer, data)

        Files.writeString(tmp, text)

        Files.move(
            tmp,
            file,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
        )
    }
}