package non.era.aisdme.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextHelperTest {
    private val path = "C:\\Proj\\ideaProj\\build\\proj\\main.asm"

    // Текст влезает целиком: возвращается без изменений
    @Test
    fun fittingTextIsUnchanged() {
        assertEquals(path, TextHelper.middleEllipsis(path) { it.length <= path.length })
    }

    // Не влезает: берётся самый длинный вариант, который влезает, и он ровно в границе
    @Test
    fun longestFittingVariantIsChosen() {
        val res = TextHelper.middleEllipsis(path) { it.length <= 20 }

        assertEquals(20, res.length)
    }

    // Середина заменена многоточием, конец пути (имя файла) сохранён, начало тоже
    @Test
    fun endsAreKeptAndMiddleIsCut() {
        val res = TextHelper.middleEllipsis(path) { it.length <= 20 }

        assertTrue(res.contains("…"))
        assertTrue(res.startsWith("C:\\"))
        assertTrue(res.endsWith("main.asm"))
    }

    // Хвосту достаётся больше символов, чем началу
    @Test
    fun tailGetsMoreThanHead() {
        val res = TextHelper.middleEllipsis(path) { it.length <= 20 }
        val (head, tail) = res.split("…")

        assertTrue(tail.length > head.length)
    }

    // Не влезает ничего, кроме многоточия: остаётся только оно
    @Test
    fun onlyEllipsisWhenNothingElseFits() {
        assertEquals("…", TextHelper.middleEllipsis(path) { it.length <= 1 })
    }

    // Пустой текст влезает всегда
    @Test
    fun emptyTextStaysEmpty() {
        assertEquals("", TextHelper.middleEllipsis("") { true })
    }
}
