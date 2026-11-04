package non.era.aisdme.utils

import java.nio.file.Path

object PathHelper {
    fun pathToStrFormat(path: Path) : String {
        val susHome = System.getProperty("user.home")
        val rootProj = path.toString()
        return if (rootProj.startsWith(susHome)) {
            "~" + rootProj.removePrefix(susHome)
        } else {
            rootProj
        }
    }
}