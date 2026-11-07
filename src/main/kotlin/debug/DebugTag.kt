package non.era.aisdme.debug

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import non.era.aisdme.BuildConfig
import kotlin.math.roundToInt

/** Видимые помеченные элементы окна: имя и границы. Пишется и читается на ui-потоке. */
object DebugTag {
    private class Entry(val name: String, val rect: Rect)

    private val items = LinkedHashMap<Any, Entry>()

    fun put(token: Any, name: String, rect: Rect) {
        items[token] = Entry(name, rect)
    }

    fun remove(token: Any) {
        items.remove(token)
    }

    fun lines(): List<String> = items.values.map { "${it.name} ${center(it)}" }

    /** Центр элемента в пикселях окна. Имя — точное или часть имени; не однозначно — ошибка со списком */
    fun point(query: String): Pair<Int, Int> {
        val all = items.values.toList()
        val found = all.filter { it.name == query }.ifEmpty { all.filter { it.name.contains(query) } }
        val entry = found.singleOrNull() ?: error("элементов «$query»: ${found.size}; видимые: ${all.map { it.name }}")
        return entry.rect.center.x.roundToInt() to entry.rect.center.y.roundToInt()
    }

    private fun center(e: Entry) = "@${e.rect.center.x.roundToInt()},${e.rect.center.y.roundToInt()}"
}

/** Метка для пульта отладки: элемент виден в `state` и доступен команде `tap`. В release ничего не делает. */
fun Modifier.debugTag(name: String): Modifier = if (!BuildConfig.DEBUG) this else composed {
    val token = remember { Any() }
    DisposableEffect(token) { onDispose { DebugTag.remove(token) } }
    Modifier.onGloballyPositioned { DebugTag.put(token, name, it.boundsInWindow()) }
}