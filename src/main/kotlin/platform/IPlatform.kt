package non.era.aisdme.platform

import java.io.File
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

interface IPlatform {
    fun configRoot(): Path //папка, в которой приложения ОС хранят свои конфиги

    val watchSubtree: Boolean //true — registerWatch на корне ловит события всего поддерева
    fun registerWatch(dir: Path, service: WatchService, kinds: Array<WatchEvent.Kind<*>>): WatchKey

    val fileManagerName: String
    fun showInFileManager(file: File)

    val clipboardCutNative: String //формат буфера, которым файловый менеджер ОС помечает «вырезано»
    fun clipboardCutBytes(): ByteArray
    fun isClipboardCut(bytes: ByteArray): Boolean
}