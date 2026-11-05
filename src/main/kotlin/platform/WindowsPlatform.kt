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

    private const val DROPEFFECT_MOVE = 2

    override val clipboardCutNative = "Preferred DropEffect"

    override fun clipboardCutBytes() = byteArrayOf(DROPEFFECT_MOVE.toByte(), 0, 0, 0) //DWORD, little-endian

    override fun isClipboardCut(bytes: ByteArray) =
        bytes.isNotEmpty() && bytes[0].toInt() and DROPEFFECT_MOVE != 0 //при копировании Explorer пишет 5: COPY | LINK
}