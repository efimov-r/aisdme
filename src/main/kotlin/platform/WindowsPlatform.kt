package non.era.aisdme.platform

import java.nio.file.Path
import java.nio.file.Paths

object WindowsPlatform : IPlatform {
    override fun configRoot(): Path =
        Paths.get(System.getenv("APPDATA") ?: error("APPDATA not found"))
}