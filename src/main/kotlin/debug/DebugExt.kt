package non.era.aisdme.debug

import non.era.aisdme.di.AppController
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime

private const val TIME_STEP_MS = 1000L //правка заведомо новее прежней: отпечаток файла в приложении — время и размер

/**
 * Правки файлов проекта «из другой программы»; пути — от корня проекта, за его пределы нельзя:
 * `write путь | текст` (`\n` — перевод строки), `delete путь`, `mkdir путь`, `rename старый | новый`.
 */
class DebugExt(private val app: AppController) {
    fun run(rest: String) {
        val action = rest.substringBefore(' ')
        val args = rest.substringAfter(' ', "").split(" | ", limit = 2)
        val path = resolve(args[0])

        when (action) {
            "write" -> write(path, args.getOrElse(1) { "" }.replace("\\n", "\n"))
            "delete" -> path.toFile().deleteRecursively()
            "mkdir" -> Files.createDirectories(path)
            "rename" -> Files.move(path, resolve(args.getOrElse(1) { error("нужен новый путь") }))
            else -> error("неизвестное действие $action")
        }
    }

    private fun write(path: Path, text: String) {
        val old = if (Files.exists(path)) Files.getLastModifiedTime(path).toMillis() else 0L
        Files.createDirectories(path.parent)
        Files.writeString(path, text)
        Files.setLastModifiedTime(path, FileTime.fromMillis(maxOf(System.currentTimeMillis(), old + TIME_STEP_MS)))
    }

    private fun resolve(relative: String): Path {
        val root = app.currentProject.root ?: error("проект не открыт")
        val path = root.resolve(relative.trim()).normalize()
        require(path.startsWith(root) && path != root) { "путь вне проекта: $relative" }
        return path
    }
}