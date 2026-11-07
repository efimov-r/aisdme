package non.era.aisdme.debug

import kotlinx.coroutines.delay
import org.jetbrains.skiko.SkiaLayer
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import javax.swing.SwingUtilities
import kotlin.math.roundToInt

private const val EVENT_DELAY_MS = 20L
private const val DRAG_STEPS = 10

private val KEY_ALIASES = mapOf(
    "esc" to "ESCAPE",
    "del" to "DELETE",
    "ins" to "INSERT",
    "return" to "ENTER",
    "pgup" to "PAGE_UP",
    "pgdn" to "PAGE_DOWN",
)

/** Синтетические события AWT прямо в окно: настоящая мышь и клавиатура не задействуются, фокус не нужен. */
class DebugInput(private val layer: SkiaLayer) {
    private val target: Component = SwingUtilities.getDeepestComponentAt(layer, 1, 1) ?: layer //вложенный холст

    suspend fun move(x: Int, y: Int) = mouse(MouseEvent.MOUSE_MOVED, x, y, MouseEvent.NOBUTTON)

    suspend fun click(x: Int, y: Int, button: String, count: Int) {
        val b = buttonOf(button)
        move(x, y)
        for (i in 1..count) {
            mouse(MouseEvent.MOUSE_PRESSED, x, y, b, i)
            mouse(MouseEvent.MOUSE_RELEASED, x, y, b, i)
            mouse(MouseEvent.MOUSE_CLICKED, x, y, b, i)
        }
    }

    suspend fun drag(x1: Int, y1: Int, x2: Int, y2: Int) {
        move(x1, y1)
        mouse(MouseEvent.MOUSE_PRESSED, x1, y1, MouseEvent.BUTTON1, 1)
        for (i in 1..DRAG_STEPS) {
            mouse(MouseEvent.MOUSE_DRAGGED, x1 + (x2 - x1) * i / DRAG_STEPS, y1 + (y2 - y1) * i / DRAG_STEPS,
                MouseEvent.BUTTON1)
        }
        mouse(MouseEvent.MOUSE_RELEASED, x2, y2, MouseEvent.BUTTON1, 1)
    }

    suspend fun scroll(x: Int, y: Int, amount: Int) { //amount > 0 — вниз
        target.dispatchEvent(MouseWheelEvent(target, MouseEvent.MOUSE_WHEEL, now(), 0, dip(x), dip(y), 0, false,
            MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, amount))
        delay(EVENT_DELAY_MS)
    }

    suspend fun type(text: String) {
        for (ch in text) {
            target.dispatchEvent(KeyEvent(target, KeyEvent.KEY_TYPED, now(), 0, KeyEvent.VK_UNDEFINED, ch))
            delay(EVENT_DELAY_MS)
        }
    }

    suspend fun key(combo: String) {
        val parts = combo.lowercase().split('+')
        val mods = parts.dropLast(1).fold(0) { acc, p -> acc or modifierOf(p) }
        val code = keyCodeOf(parts.last())
        for (id in listOf(KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED)) {
            target.dispatchEvent(KeyEvent(target, id, now(), mods, code, KeyEvent.CHAR_UNDEFINED))
            delay(EVENT_DELAY_MS)
        }
    }

    private suspend fun mouse(id: Int, x: Int, y: Int, button: Int, count: Int = 0) {
        val held = id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_DRAGGED
        val mods = if (held) downMask(button) else 0
        target.dispatchEvent(MouseEvent(target, id, now(), mods, dip(x), dip(y), count, false, button))
        delay(EVENT_DELAY_MS)
    }

    private fun dip(px: Int) = (px / layer.contentScale).roundToInt() //скриншот в пикселях, события — в dip
}

private fun now() = System.currentTimeMillis()

private fun buttonOf(name: String) = when (name) {
    "left" -> MouseEvent.BUTTON1
    "middle" -> MouseEvent.BUTTON2
    "right" -> MouseEvent.BUTTON3
    else -> error("неизвестная кнопка $name")
}

private fun downMask(button: Int) = when (button) {
    MouseEvent.BUTTON1 -> InputEvent.BUTTON1_DOWN_MASK
    MouseEvent.BUTTON2 -> InputEvent.BUTTON2_DOWN_MASK
    else -> InputEvent.BUTTON3_DOWN_MASK
}

private fun modifierOf(name: String) = when (name) {
    "ctrl" -> InputEvent.CTRL_DOWN_MASK
    "shift" -> InputEvent.SHIFT_DOWN_MASK
    "alt" -> InputEvent.ALT_DOWN_MASK
    "meta" -> InputEvent.META_DOWN_MASK
    else -> error("неизвестный модификатор $name")
}

private fun keyCodeOf(name: String): Int {
    val field = "VK_${KEY_ALIASES[name] ?: name.uppercase()}"
    return runCatching { KeyEvent::class.java.getField(field).getInt(null) }.getOrNull()
        ?: error("неизвестная клавиша $name")
}