package non.era.aisdme.debug

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import non.era.aisdme.di.AppController
import org.jetbrains.skiko.SkiaLayer
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.SwingUtilities

private const val CROP_VALUES = 4 //x, y, ширина, высота
private const val LOG_LINES = 30
private val INFO_COMMANDS = setOf("state", "text", "dump", "log")
private val APP_COMMANDS = setOf("wait", "close", "throw")

/**
 * Команды в файле — по одной в строке, координаты — в пикселях скриншота:
 * `shot [имя]`, `wait мс`, `move x y`, `click x y [left|right|middle] [число]`, `drag x1 y1 x2 y2`,
 * `drag A -> B` и `dragby A dx dy` (A, B — `node имя`, `tap имя` или `x y`; `DebugDrag`),
 * `mark имя` и `expect moved имя dx dy допуск` — сдвиг помеченного элемента (разделители панелей),
 * `scroll x y шаги`, `type текст`, `key ctrl+shift+s`, `state` (дерево, вкладки, тема), `text [left|right]`,
 * `node [right] [double] имя` (клик по узлу дерева), `arrow имя` (раскрыть или свернуть папку),
 * `log [строк]` (хвост лога приложения), `dump` (дерево компонентов AWT), `close` (закрыть окно),
 * `shot имя x y ширина высота` — вырезка области,
 * `expect [not] источник подстрока` и `waitfor [not] источник мс подстрока` — проверка и ожидание
 * (источники: state, text, text-right, log, exists; подробнее — `DebugCheck`),
 * `expect inside A in B` и `waitfor inside мс A in B` — элемент A целиком внутри элемента B (по меткам),
 * `expect apart A and B` — элементы не пересекаются (`DebugGeometry`),
 * `hover имя` — навести мышь на помеченный элемент, `throw текст` — необработанное исключение на ui-потоке,
 * `tap [right] [double] имя` — клик по помеченному элементу из раздела `ui` в `state` (`DebugTag`);
 * `tap A within B` — клик по видимой части A внутри B (например, `tap tab:main.asm within tabs:left`),
 * `ext write|delete|mkdir|rename …` — правки файлов проекта «снаружи» (`DebugExt`), `focus` — окно получило фокус,
 * строки с `#` в начале пропускаются.
 * Первая ошибка прерывает файл.
 */
class DebugCommands(private val window: ComposeWindow, private val app: AppController, private val dir: File) {
    private val check = DebugCheck(app)
    private val ext = DebugExt(app)
    private val drag = DebugDrag(app)

    suspend fun handle(file: File) {
        val id = file.nameWithoutExtension
        val text = withContext(Dispatchers.IO) { file.readText().also { file.delete() } }
        val out = StringBuilder()

        for (line in text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }) {
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
            in INFO_COMMANDS -> "\n" + info(cmd, rest)
            "expect" -> check.verify(rest, withTimeout = false)
            "waitfor" -> check.verify(rest, withTimeout = true)
            in APP_COMMANDS -> "ok".also { app(cmd, rest) }
            else -> "ok".also { input(DebugInput(layer), cmd, rest) }
        }
    }

    private suspend fun app(cmd: String, rest: String) {
        when (cmd) {
            "wait" -> delay(rest.toLong())
            "close" -> window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
            else -> SwingUtilities.invokeLater { error(rest) } //необработанное исключение на ui-потоке
        }
    }

    private fun info(cmd: String, rest: String): String = when (cmd) {
        "state" -> DebugState.dump(app)
        "text" -> DebugState.text(app, rest)
        "log" -> DebugLog.tail(rest.toIntOrNull() ?: LOG_LINES)
        else -> dumpComponents(window)
    }

    //[right] [double] имя; point — точка элемента по имени, dx — сдвиг от неё
    private suspend fun clickPoint(input: DebugInput, rest: String, dx: Int, point: (String) -> Pair<Int, Int>) {
        val tokens = rest.split(' ')
        val mods = tokens.takeWhile { it == "right" || it == "double" }
        val (x, y) = point(tokens.drop(mods.size).joinToString(" "))
        val button = if ("right" in mods) "right" else "left"
        input.click(x + dx, y, button, if ("double" in mods) 2 else 1)
    }

    private suspend fun input(input: DebugInput, cmd: String, rest: String) {
        when (cmd) {
            "type" -> input.type(rest)
            "key" -> input.key(rest)
            "node" -> clickPoint(input, rest, DebugState.ROW_DX) { DebugState.nodePoint(app, it) }
            "arrow" -> clickPoint(input, rest, -DebugState.ARROW_DX) { DebugState.nodePoint(app, it) }
            "tap" -> clickPoint(input, rest, 0, DebugTag::point)
            "drag" -> if ("->" in rest) drag.between(input, rest) else pointer(input, cmd, rest)
            "dragby" -> drag.by(input, rest)
            "mark" -> check.mark(rest)
            "ext" -> ext.run(rest)
            "focus" -> window.windowFocusListeners.forEach { //напрямую: AWT гасит событие у окна без настоящего фокуса
                it.windowGainedFocus(WindowEvent(window, WindowEvent.WINDOW_GAINED_FOCUS))
            }
            else -> pointer(input, cmd, rest)
        }
    }

    private suspend fun pointer(input: DebugInput, cmd: String, rest: String) { //команды с координатами
        val args = rest.split(' ').filter { it.isNotEmpty() }
        val nums = args.mapNotNull { it.toIntOrNull() }
        val word = args.firstOrNull { it.toIntOrNull() == null }

        when (cmd) {
            "move" -> nums.let { (x, y) -> input.move(x, y) }
            "hover" -> DebugTag.point(rest).let { (x, y) -> input.move(x, y) } //на помеченный элемент
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