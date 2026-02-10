package non.era.aisdme.files

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.IControler
import non.era.aisdme.Log
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor.stringFlavor
import java.awt.datatransfer.StringSelection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class FilesController : IControler {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _errors = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val errors: SharedFlow<String> = _errors

    private fun <T> safeLaunch(
        operation: suspend () -> T
    ): Job {
        return scope.launch {
            runCatching {
                operation()
            }.onFailure { e ->
                Log.e("Error ${e.message}")
                withContext(Dispatchers.Swing) {
                    _errors.emit(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun createFile(path: Path) = safeLaunch {
            Files.createFile(path)
    }

    fun createDirectory(path: Path) = safeLaunch {
        Files.createDirectory(path)
    }

    fun delete(path: Path) = safeLaunch {
        Files.deleteIfExists(path)
    }

    fun deleteRecursive(path: Path) = safeLaunch{
        Files.walk(path)
            .sorted(Comparator.reverseOrder())
            .forEach(Files::delete)
    }

    fun copy(src: Path, dst: Path) = safeLaunch {
        Files.copy(
            src,
            dst,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES
        )
    }

    fun copyRecursive(src: Path, dst: Path) = safeLaunch {
        Files.walk(src).forEach { source ->
            val target = dst.resolve(src.relativize(source))
            if (Files.isDirectory(source)) {
                Files.createDirectories(target)
            } else {
                Files.copy(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
                )
            }
        }
    }

    fun move(src: Path, dst: Path) = safeLaunch {
        Files.move(
            src,
            dst,
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    fun copyToClipboard(paths: List<Path>, cut: Boolean) = safeLaunch {
        val prefix = if (cut) "cut:" else "copy:"
        val data = prefix + paths.joinToString("\n") { it.toAbsolutePath().toString() }

        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(StringSelection(data), null)
    }

    fun pasteFromClipboard(targetDir: Path) = safeLaunch {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val content = clipboard.getData(stringFlavor) as String

        val lines = content.lines()
        val isCut = lines.first().startsWith("cut:")
        val paths = lines
            .drop(1)
            .map { Paths.get(it) }

        for (src in paths) {
            val dst = targetDir.resolve(src.fileName)

            if (Files.isDirectory(src)) {
                copyRecursive(src, dst)
            } else {
                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)
            }

            if (isCut) {
                deleteRecursive(src)
            }
        }
    }

    override fun dispose() {
        scope.cancel()
    }
}