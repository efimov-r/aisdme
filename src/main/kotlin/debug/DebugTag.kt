package non.era.aisdme.debug

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize
import non.era.aisdme.BuildConfig
import kotlin.math.roundToInt

/** Видимые помеченные элементы окна: имя и границы. Пишется и читается на ui-потоке. */
object DebugTag {
    //bounds читаются при запросе: элемент, вышедший из раскладки (вкладка за краем панели), не оставляет старых границ
    private class Entry(val name: String, val bounds: () -> Rect?) {
        val rect: Rect get() = bounds() ?: Rect.Zero
    }

    private val items = LinkedHashMap<Any, Entry>()

    fun put(token: Any, name: String, rect: Rect) = put(token, name) { rect }

    fun put(token: Any, name: String, bounds: () -> Rect?) {
        items[token] = Entry(name, bounds)
    }

    fun remove(token: Any) {
        items.remove(token)
    }

    private fun actual() = items.values.filter { it.bounds() != null }

    fun lines(): List<String> = actual().map { "${it.name} ${center(it)}" }

    /**
     * Центр элемента в пикселях окна. Имя — точное или часть имени; не однозначно — ошибка со списком.
     * `A within B` — центр видимой части A внутри B (обрезанная вкладка: центр самой вкладки может лежать вне панели)
     */
    fun point(query: String): Pair<Int, Int> {
        val names = query.split(" within ", limit = 2)
        val area = if (names.size == 2) rect(names[0]).intersect(rect(names[1])) else rect(query)
        require(area.width > 0 && area.height > 0) { "«$query»: видимой части нет" }
        return area.center.let { it.x.roundToInt() to it.y.roundToInt() }
    }

    /** Границы элемента в пикселях окна, имя как у `point` */
    fun rect(query: String): Rect {
        val all = actual()
        val found = all.filter { it.name == query }.ifEmpty { all.filter { it.name.contains(query) } }
        val entry = found.singleOrNull() ?: error("элементов «$query»: ${found.size}; видимые: ${all.map { it.name }}")
        return entry.rect
    }

    private fun center(e: Entry) = "@${e.rect.center.x.roundToInt()},${e.rect.center.y.roundToInt()}"
}

/** Метка для пульта отладки: элемент виден в `state` и доступен команде `tap`. В release ничего не делает. */
fun Modifier.debugTag(name: String): Modifier = if (!BuildConfig.DEBUG) this else composed {
    val token = remember { Any() }
    DisposableEffect(token) { onDispose { DebugTag.remove(token) } }
    Modifier.onGloballyPositioned { coords ->
        //не boundsInWindow: он обрезает по родителю, и вкладка за краем панели выглядела бы целой
        DebugTag.put(token, name) {
            coords.takeIf { it.isAttached }?.let { Rect(it.positionInWindow(), it.size.toSize()) }
        }
    }
}