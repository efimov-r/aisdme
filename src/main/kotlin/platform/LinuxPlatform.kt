package non.era.aisdme.platform

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

object LinuxPlatform : IPlatform {
    override fun configRoot(): Path =
        Paths.get(System.getProperty("user.home"), ".config")

    override val watchSubtree = false //FILE_TREE не поддерживается

    override fun registerWatch(dir: Path, service: WatchService, kinds: Array<WatchEvent.Kind<*>>): WatchKey =
        dir.register(service, *kinds)
}