package non.era.aisdme.platform

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

object LinuxPlatform : IPlatform {
    private val fileManagers = linkedMapOf(
        "dolphin" to "Dolphin",
        "nautilus" to "Nautilus",
        "thunar" to "Thunar",
        "pcmanfm" to "PCManFM",
    )

    private val fileManager: String? by lazy {
        val path = System.getenv("PATH").orEmpty().split(File.pathSeparator)
        fileManagers.keys.firstOrNull { name -> path.any { File(it, name).canExecute() } }
    }

    override fun configRoot(): Path =
        Paths.get(System.getProperty("user.home"), ".config")

    override val watchSubtree = false //FILE_TREE не поддерживается

    override fun registerWatch(dir: Path, service: WatchService, kinds: Array<WatchEvent.Kind<*>>): WatchKey =
        dir.register(service, *kinds)

    override val fileManagerName: String
        get() = fileManagers[fileManager] ?: "File Manager"

    override fun showInFileManager(file: File) {
        val fm = fileManager
        val dir = if (file.isDirectory) file else file.parentFile

        val command = when (fm) {
            "dolphin", "nautilus" -> listOf(fm, "--select", file.absolutePath)
            null -> listOf("xdg-open", dir.absolutePath)
            else -> listOf(fm, dir.absolutePath) //thunar и pcmanfm не умеют выделять файл
        }
        ProcessBuilder(command).start()
    }
}