package non.era.aisdme.debug

import kotlinx.coroutines.delay
import non.era.aisdme.di.AppController
import java.nio.file.Files
import kotlin.math.abs

private const val CHECK_POLL_MS = 50L
private const val MOVE_WAIT_MS = 1000L //раскладка догоняет перетаскивание не сразу
private const val MOVED_NUMBERS = 3 //dx dy допуск
private const val SHOWN_CHARS = 400 //сколько показать в сообщении о невыполненном условии

/**
 * `expect [not] источник подстрока` и `waitfor [not] источник мс подстрока`.
 * Источники: state, text, text-right, log, exists (подстрока — путь от корня проекта; есть ли такой файл).
 * `mark имя` запоминает центр помеченного элемента, `expect moved имя dx dy допуск` проверяет сдвиг от метки.
 */
class DebugCheck(private val app: AppController) {
    private val marks = mutableMapOf<String, Pair<Int, Int>>()

    fun mark(name: String) {
        marks[name] = DebugTag.point(name)
    }

    suspend fun verify(rest: String, withTimeout: Boolean): String {
        if (rest.startsWith("moved ")) return moved(rest.removePrefix("moved "))
        val negate = rest.startsWith("not ")
        val parts = rest.removePrefix("not ").split(' ', limit = if (withTimeout) 3 else 2)
        val source = parts[0]
        val timeout = if (withTimeout) parts[1].toLong() else 0L
        val needle = parts.last()
        val start = System.currentTimeMillis()

        while (true) {
            //ошибка чтения (например, вкладка ещё грузится) — тоже содержимое: ждём дальше
            val actual = runCatching { read(source, needle) }.getOrElse { it.message.orEmpty() }
            if (actual.contains(needle) != negate) return "ok (${System.currentTimeMillis() - start} мс)"
            if (System.currentTimeMillis() - start >= timeout) {
                val verb = if (negate) "содержит" else "не содержит"
                error("$source $verb «$needle»; сейчас: ${actual.take(SHOWN_CHARS)}")
            }
            delay(CHECK_POLL_MS)
        }
    }

    private suspend fun moved(rest: String): String { //имя dx dy допуск
        val tokens = rest.split(' ')
        val (dx, dy, tolerance) = tokens.takeLast(MOVED_NUMBERS).map { it.toInt() }
        val name = tokens.dropLast(MOVED_NUMBERS).joinToString(" ")
        val before = marks[name] ?: error("нет метки «$name»: сначала mark")
        val start = System.currentTimeMillis()

        while (true) {
            val now = DebugTag.point(name)
            val shift = now.first - before.first to now.second - before.second
            if (abs(shift.first - dx) <= tolerance && abs(shift.second - dy) <= tolerance) return "ok (сдвиг $shift)"
            if (System.currentTimeMillis() - start >= MOVE_WAIT_MS) error("«$name» сдвинут на $shift, ждали ($dx, $dy)")
            delay(CHECK_POLL_MS)
        }
    }

    private fun read(source: String, needle: String): String = when (source) {
        "state" -> DebugState.dump(app)
        "text" -> DebugState.text(app, "left")
        "text-right" -> DebugState.text(app, "right")
        "log" -> DebugLog.all()
        "exists" -> if (exists(needle)) needle else ""
        else -> error("неизвестный источник $source")
    }

    private fun exists(relative: String): Boolean {
        val root = app.currentProject.root ?: error("проект не открыт")
        return Files.exists(root.resolve(relative).normalize())
    }
}