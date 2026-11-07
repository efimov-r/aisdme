package non.era.aisdme.debug

import kotlinx.coroutines.delay
import non.era.aisdme.di.AppController

private const val CHECK_POLL_MS = 50L
private const val SHOWN_CHARS = 400 //сколько показать в сообщении о невыполненном условии

/**
 * `expect [not] источник подстрока` и `waitfor [not] источник мс подстрока`.
 * Источники: state, text, text-right, log.
 */
class DebugCheck(private val app: AppController) {
    suspend fun verify(rest: String, withTimeout: Boolean): String {
        val negate = rest.startsWith("not ")
        val parts = rest.removePrefix("not ").split(' ', limit = if (withTimeout) 3 else 2)
        val source = parts[0]
        val timeout = if (withTimeout) parts[1].toLong() else 0L
        val needle = parts.last()
        val start = System.currentTimeMillis()

        while (true) {
            val actual = runCatching { read(source) }.getOrElse { it.message.orEmpty() } //вкладка ещё грузится — ждём
            if (actual.contains(needle) != negate) return "ok (${System.currentTimeMillis() - start} мс)"
            if (System.currentTimeMillis() - start >= timeout) {
                val verb = if (negate) "содержит" else "не содержит"
                error("$source $verb «$needle»; сейчас: ${actual.take(SHOWN_CHARS)}")
            }
            delay(CHECK_POLL_MS)
        }
    }

    private fun read(source: String): String = when (source) {
        "state" -> DebugState.dump(app)
        "text" -> DebugState.text(app, "left")
        "text-right" -> DebugState.text(app, "right")
        "log" -> DebugLog.all()
        else -> error("неизвестный источник $source")
    }
}