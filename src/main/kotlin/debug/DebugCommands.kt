package non.era.aisdme.debug

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.SkiaLayer
import java.awt.event.WindowEvent
import java.io.File

/**
 * Команды в файле — по одной в строке, координаты — в пикселях скриншота:
 * `shot [имя]`, `wait мс`, `move x y`, `click x y [left|right|middle] [число]`, `drag x1 y1 x2 y2`,
 * `scroll x y шаги`, `type текст`, `key ctrl+shift+s`, `dump` (дерево компонентов), `close` (закрыть окно).
 * Первая ошибка прерывает файл.
 */
class DebugCommands(private val window: ComposeWindow, private val dir: File) {
    suspend fun handle(file: File) {
        val id = file.nameWithoutExtension
        val text = withContext(Dispatchers.IO) { file.readText().also { file.delete() } }
        val out = StringBuilder()

        for (line in text.lines().map { it.trim() }.filter { it.isNotEmpty() }) {
            val res = runCatching { execute(id, line) }.getOrElse { "error: ${it.message ?: it}" }
            out.appendLine("$line -> $res")
            if (res.startsWith("error")) break
        }

        withContext(Dispatchers.IO) {
            val tmp = File(dir, "$id.tmp")
            tmp.writeText(out.toString())
            tmp.renameTo(File(dir, "$id.out"))
        }
    }

    private suspend fun execute(id: String, line: String): String {
        val cmd = line.substringBefore(' ')
        val rest = line.substringAfter(' ', "")
        val layer = findSkiaLayer(window) ?: error("в окне нет SkiaLayer")

        return when (cmd) {
            "shot" -> shot(layer, File(dir, "${rest.ifEmpty { id }}.png"))
            "dump" -> "\n" + dumpComponents(window)
            "wait" -> "ok".also { delay(rest.toLong()) }
            "close" -> "ok".also { window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING)) }
            else -> "ok".also { input(DebugInput(layer), cmd, rest) }
        }
    }

    private suspend fun input(input: DebugInput, cmd: String, rest: String) {
        val args = rest.split(' ').filter { it.isNotEmpty() }
        val nums = args.mapNotNull { it.toIntOrNull() }
        val word = args.firstOrNull { it.toIntOrNull() == null }

        when (cmd) {
            "move" -> nums.let { (x, y) -> input.move(x, y) }
            "click" -> nums.let { (x, y) -> input.click(x, y, word ?: "left", nums.getOrNull(2) ?: 1) }
            "drag" -> nums.chunked(2).let { (from, to) -> input.drag(from[0], from[1], to[0], to[1]) }
            "scroll" -> nums.let { (x, y, amount) -> input.scroll(x, y, amount) }
            "type" -> input.type(rest)
            "key" -> input.key(rest)
            else -> error("неизвестная команда $cmd")
        }
    }

    private fun shot(layer: SkiaLayer, file: File): String {
        val bytes = layer.png() ?: error("скриншот не получен")
        file.writeBytes(bytes)
        return "ok ${file.absolutePath} (окно ${layer.width}x${layer.height} dip, scale ${layer.contentScale})"
    }
}