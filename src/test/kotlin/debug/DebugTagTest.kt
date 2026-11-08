package non.era.aisdme.debug

import androidx.compose.ui.geometry.Rect
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DebugTagTest {
    private val tokens = mutableListOf<Any>()

    private fun put(name: String, rect: Rect) {
        val token = Any().also { tokens.add(it) }
        DebugTag.put(token, name, rect)
    }

    @AfterTest
    fun cleanup() {
        tokens.forEach { DebugTag.remove(it) }
    }

    // Точное имя важнее части имени: «tab:a» не путается с «tab:a.txt»
    @Test
    fun exactNameWinsOverPartialMatch() {
        put("tab:a", Rect(0f, 0f, 10f, 10f))
        put("tab:a.txt", Rect(20f, 0f, 30f, 10f))

        assertEquals(Rect(0f, 0f, 10f, 10f), DebugTag.rect("tab:a"))
    }

    // Часть имени находит единственный элемент, центр считается по его границам
    @Test
    fun partialNameFindsSingleElement() {
        put("tab:main.asm#0", Rect(100f, 0f, 200f, 40f))

        assertEquals(Rect(100f, 0f, 200f, 40f), DebugTag.rect("tab:main"))
        assertEquals(150 to 20, DebugTag.point("tab:main"))
    }

    // Неоднозначное имя — ошибка со списком видимых, как и отсутствующее
    @Test
    fun ambiguousOrMissingNameFails() {
        put("tab:a.txt#1", Rect(0f, 0f, 10f, 10f))
        put("tab:a.txt#2", Rect(20f, 0f, 30f, 10f))

        assertFailsWith<IllegalStateException> { DebugTag.rect("tab:a.txt") }
        assertFailsWith<IllegalStateException> { DebugTag.rect("tab:zzz") }
    }
}
