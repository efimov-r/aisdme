package non.era.aisdme.platform

import com.sun.nio.file.ExtendedWatchEventModifier
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

object WindowsPlatform : IPlatform {
    override fun configRoot(): Path =
        Paths.get(System.getenv("APPDATA") ?: error("APPDATA not found"))

    override val watchSubtree = true //каждая зарегистрированная папка остаётся открытой, а открытая подпапка не даёт переименовать родителя

    override fun registerWatch(dir: Path, service: WatchService, kinds: Array<WatchEvent.Kind<*>>): WatchKey =
        dir.register(service, kinds, ExtendedWatchEventModifier.FILE_TREE)

    override val fileManagerName = "Explorer"

    override fun showInFileManager(file: File) {
        ProcessBuilder("explorer", "/select,", file.absolutePath).start()
    }
}