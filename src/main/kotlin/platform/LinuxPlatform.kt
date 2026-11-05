package non.era.aisdme.platform

import java.nio.file.Path
import java.nio.file.Paths

object LinuxPlatform : IPlatform {
    override fun configRoot(): Path =
        Paths.get(System.getProperty("user.home"), ".config")
}