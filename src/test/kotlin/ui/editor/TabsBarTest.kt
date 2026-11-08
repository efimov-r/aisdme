package non.era.aisdme.ui.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class TabsBarTest {
    // Вкладка целиком в видимой области: панель не двигается
    @Test
    fun visibleTabDoesNotScroll() {
        assertEquals(0, tabScrollShift(offset = 100, size = 110, start = 0, end = 400, beyondEnd = false))
    }

    // Вкладка вплотную к краям: тоже видна, не двигаем
    @Test
    fun tabTouchingEdgesDoesNotScroll() {
        assertEquals(0, tabScrollShift(offset = 0, size = 400, start = 0, end = 400, beyondEnd = false))
    }

    // Вкладка обрезана справа: сдвиг вправо на недостающую часть
    @Test
    fun tabCutOnRightScrollsRight() {
        assertEquals(30, tabScrollShift(offset = 320, size = 110, start = 0, end = 400, beyondEnd = false))
    }

    // Вкладка обрезана слева: сдвиг влево
    @Test
    fun tabCutOnLeftScrollsLeft() {
        assertEquals(-25, tabScrollShift(offset = -25, size = 110, start = 0, end = 400, beyondEnd = false))
    }

    // Вкладка ушла из раскладки справа: после прыжка к ней она у левого края, сдвигаем её к правому
    @Test
    fun tabBeyondEndIsAlignedToEnd() {
        assertEquals(-290, tabScrollShift(offset = 0, size = 110, start = 0, end = 400, beyondEnd = true))
    }

    // Последняя вкладка уже у правого края после прыжка (список упёрся в конец): сдвиг не нужен
    @Test
    fun lastTabBeyondEndStaysWhenAlreadyAtEnd() {
        assertEquals(0, tabScrollShift(offset = 290, size = 110, start = 0, end = 400, beyondEnd = true))
    }

    // Отступы области (контент не доходит до края): считаем от start и end, а не от краёв окна
    @Test
    fun contentPaddingIsRespected() {
        assertEquals(20, tabScrollShift(offset = 330, size = 110, start = 10, end = 420, beyondEnd = false))
    }
}
