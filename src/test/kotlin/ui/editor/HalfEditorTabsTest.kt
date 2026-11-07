package non.era.aisdme.ui.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HalfEditorTabsTest {
    private var savedTabs: List<Int>? = null
    private var savedActive: Int? = null
    private var changed = 0

    private val half = HalfEditorTabs(
        updateTabs = { savedTabs = it.toList() },
        updateActive = { savedActive = it },
        onChanged = { changed++ },
    )

    private fun open(vararg ids: Int) = ids.forEach { half.activateTab(it) }

    // новая вкладка — в конец и активная, половина в фокусе, репозиторий и onChanged уведомлены
    @Test
    fun activateNewTabAddsItAndMakesActive() {
        open(1)
        half.inFocus = false

        half.activateTab(2)

        assertEquals(listOf(1, 2), half.listTabs.toList())
        assertEquals(2, half.activeTabId)
        assertEquals(listOf(1, 2), savedTabs)
        assertEquals(2, savedActive)
        assertTrue(half.inFocus)
        assertEquals(2, changed)
    }

    // уже открытая вкладка не дублируется и не меняет место, только становится активной
    @Test
    fun activateExistingTabKeepsOrder() {
        open(1, 2, 3)

        half.activateTab(1)

        assertEquals(listOf(1, 2, 3), half.listTabs.toList())
        assertEquals(1, half.activeTabId)
    }

    // закрытие неактивной вкладки не меняет активную
    @Test
    fun closeInactiveTabKeepsActive() {
        open(1, 2, 3)

        half.closeTab(1)

        assertEquals(listOf(2, 3), half.listTabs.toList())
        assertEquals(3, half.activeTabId)
    }

    // закрытие активной вкладки активирует последнюю из оставшихся, а последней — никого
    @Test
    fun closeActiveTabActivatesLast() {
        open(1, 2, 3)

        half.closeTab(3)
        assertEquals(2, half.activeTabId)

        half.closeTab(2)
        half.closeTab(1)
        assertNull(half.activeTabId)
        assertNull(savedActive)
        assertEquals(emptyList(), savedTabs)
    }

    // «закрыть остальные» оставляет одну вкладку и делает её активной
    @Test
    fun closeOtherTabsKeepsOnlyGivenTab() {
        open(1, 2, 3)

        half.closeOtherTabs(2)

        assertEquals(listOf(2), half.listTabs.toList())
        assertEquals(2, half.activeTabId)
        assertEquals(2, savedActive)
    }

    // «закрыть слева»: остаётся вкладка и всё правее; активная вкладка слева заменяется на выбранную
    @Test
    fun closeLeftTabsKeepsGivenTabAndRight() {
        open(1, 2, 3, 4)

        half.closeLeftTabs(3)
        assertEquals(listOf(3, 4), half.listTabs.toList())
        assertEquals(4, half.activeTabId)

        half.activateTab(3)
        half.closeLeftTabs(4)
        assertEquals(listOf(4), half.listTabs.toList())
        assertEquals(4, half.activeTabId)
    }

    // «закрыть слева» с активной вкладкой, которая закрылась, делает активной выбранную
    @Test
    fun closeLeftTabsActivatesGivenTabIfActiveClosed() {
        open(1, 2, 3)
        half.activateTab(1)

        half.closeLeftTabs(2)

        assertEquals(listOf(2, 3), half.listTabs.toList())
        assertEquals(2, half.activeTabId)
    }

    // «закрыть справа»: остаётся вкладка и всё левее; закрылась активная — активной становится выбранная
    @Test
    fun closeRightTabsKeepsGivenTabAndLeft() {
        open(1, 2, 3, 4)

        half.closeRightTabs(2)

        assertEquals(listOf(1, 2), half.listTabs.toList())
        assertEquals(2, half.activeTabId)
    }

    // «закрыть справа» на последней вкладке ничего не закрывает
    @Test
    fun closeRightTabsOnLastTabKeepsAll() {
        open(1, 2, 3)

        half.closeRightTabs(3)

        assertEquals(listOf(1, 2, 3), half.listTabs.toList())
        assertEquals(3, half.activeTabId)
    }

    // неизвестная вкладка: «закрыть слева» и «закрыть справа» ничего не закрывают
    @Test
    fun closeSideTabsWithUnknownIdKeepAll() {
        open(1, 2)

        half.closeLeftTabs(9)
        half.closeRightTabs(9)

        assertEquals(listOf(1, 2), half.listTabs.toList())
        assertEquals(2, half.activeTabId)
    }

    // «закрыть все» очищает список и активную вкладку
    @Test
    fun closeAllTabsClearsEverything() {
        open(1, 2)

        half.closeAllTabs()

        assertEquals(emptyList(), half.listTabs.toList())
        assertNull(half.activeTabId)
        assertEquals(emptyList(), savedTabs)
        assertNull(savedActive)
    }

    // загрузка из конфига: без выбранной вкладки активной становится последняя; репозиторий не трогается
    @Test
    fun reselectProjectSetsTabsAndActive() {
        half.reselectProject(listOf(1, 2, 3), 2)
        assertEquals(listOf(1, 2, 3), half.listTabs.toList())
        assertEquals(2, half.activeTabId)

        half.reselectProject(listOf(4, 5), null)
        assertEquals(5, half.activeTabId)

        half.reselectProject(null, null)
        assertEquals(emptyList(), half.listTabs.toList())
        assertNull(half.activeTabId)
        assertNull(savedTabs)
    }

    // копия получает вкладки и активную, старая половина пустеет и больше ни о чём не сообщает
    @Test
    fun copyMovesTabsToNewHalfAndDetachesOld() {
        open(1, 2)
        var newTabs: List<Int>? = null
        var newActive: Int? = null

        val copy = half.copy({ newTabs = it.toList() }, { newActive = it })

        assertEquals(listOf(1, 2), copy.listTabs.toList())
        assertEquals(2, copy.activeTabId)
        assertEquals(listOf(1, 2), newTabs)
        assertEquals(2, newActive)
        assertEquals(emptyList(), savedTabs) //старая половина сообщила, что опустела
        assertNull(savedActive)

        val changedBefore = changed
        half.activateTab(9)
        assertEquals(emptyList(), savedTabs)
        assertEquals(changedBefore, changed)
        assertEquals(listOf(1, 2), newTabs)

        copy.activateTab(3) //onChanged у копии прежний
        assertEquals(changedBefore + 1, changed)
    }
}