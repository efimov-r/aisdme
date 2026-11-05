package non.era.aisdme.utils

import java.nio.file.Path

object PathHelper {
    fun pathToStrFormat(path: Path) : String {
        val home = Path.of(System.getProperty("user.home"))
        val rootProj = path.toString()
        return if (path.startsWith(home)) { //по частям пути: /home/user2 не внутри /home/user
            "~" + rootProj.substring(home.toString().length)
        } else {
            rootProj
        }
    }
}