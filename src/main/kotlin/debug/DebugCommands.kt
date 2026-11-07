package non.era.aisdme.debug

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import non.era.aisdme.di.AppController
import org.jetbrains.skiko.SkiaLayer
import java.awt.event.WindowEvent
import java.io.File

private const val CROP_VALUES = 4 //x, y, ширина, высота
private const val ARROW_DX = 18 //от точки узла до стрелки раскрытия папки, пиксели
private const val ROW_DX = 40 //от точки узла до имени: клик по ряду

/**
 * Команды в файле — по одной в строке, координаты — в пикселях скриншота:
 * `shot [имя]`, `wait мс`, `move x y`, `click x y [left|right|middle] [число]`, `drag x1 y1 x2 y2`,
 * `scroll x y шаги`, `type текст`, `key ctrl+shift+s`, `state` (дерево, вкладки, тема), `text [left|right]`,
 * `node [right] [double] имя` (клик по узлу дерева), `arrow имя` (раскрыть или свернуть папку),
 * `dump` (дерево компонентов AWT), `close` (закрыть окно). `shot имя x y ширина высота` — вырезка области.
 * Первая ошибка прерывает файл.
 */
class DebugCommands(private val window: ComposeWindow, private val app: AppController, private val dir: File) {
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
            "shot" -> shot(layer, rest, id)
            "state" -> "\n" + DebugState.dump(app)
            "text" -> "\n" + DebugState.text(app, rest)
            "dump" -> "\n" + dumpComponents(window)
            "wait" -> "ok".also { delay(rest.toLong()) }
            "close" -> "ok".also { window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING)) }
            else -> "ok".also { input(DebugInput(layer), cmd, rest) }
        }
    }

    private suspend fun clickNode(input: DebugInput, rest: String, arrow: Boolean) { //[right] [double] имя узла
        val tokens = rest.split(' ')
        val mods = tokens.takeWhile { it == "right" || it == "double" }
        val (x, y) = DebugState.nodePoint(app, tokens.drop(mods.size).joinToString(" "))
        val button = if ("right" in mods) "right" else "left"
        input.click(x + if (arrow) -ARROW_DX else ROW_DX, y, button, if ("double" in mods) 2 else 1)
    }

    private suspend fun input(input: DebugInput, cmd: String, rest: String) {
        when (cmd) {
            "type" -> input.type(rest)
            "key" -> input.key(rest)
            "node" -> clickNode(input, rest, arrow = false)
            "arrow" -> clickNode(input, rest, arrow = true)
            else -> pointer(input, cmd, rest)
        }
    }

    private suspend fun pointer(input: DebugInput, cmd: String, rest: String) { //команды с координатами
        val args = rest.split(' ').filter { it.isNotEmpty() }
        val nums = args.mapNotNull { it.toIntOrNull() }
        val word = args.firstOrNull { it.toIntOrNull() == null }

        when (cmd) {
            "move" -> nums.let { (x, y) -> input.move(x, y) }
            "click" -> nums.let { (x, y) -> input.click(x, y, word ?: "left", nums.getOrNull(2) ?: 1) }
            "drag" -> nums.chunked(2).let { (from, to) -> input.drag(from[0], from[1], to[0], to[1]) }
            "scroll" -> nums.let { (x, y, amount) -> input.scroll(x, y, amount) }
            else -> error("неизвестная команда $cmd")
        }
    }

    private fun shot(layer: SkiaLayer, rest: String, id: String): String { //shot [имя] [x y ширина высота]
        val args = rest.split(' ').filter { it.isNotEmpty() }
        val crop = args.mapNotNull { it.toIntOrNull() }
        val file = File(dir, "${args.firstOrNull { it.toIntOrNull() == null } ?: id}.png")
        val bytes = layer.png() ?: error("скриншот не получен")
        file.writeBytes(if (crop.size == CROP_VALUES) crop(bytes, crop) else bytes)
        return "ok ${file.absolutePath} (окно ${layer.width}x${layer.height} dip, scale ${layer.contentScale})"
    }
}