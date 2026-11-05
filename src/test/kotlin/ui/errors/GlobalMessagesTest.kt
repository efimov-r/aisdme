package non.era.aisdme.ui.errors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withTimeout
import non.era.aisdme.ui.UiController
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalMessagesTest {
    private class Source : UiController() {
        fun fail(text: String) = scope.launch { newGlobalError(text) }
    }

    private val source = Source()
    private val messages = GlobalMessages()

    @AfterEach
    fun dispose() = source.dispose()

    private fun onUi(block: () -> Unit) = runBlocking(Dispatchers.Swing) { block() }

    // ошибка из контроллера попадает в список
    @Test
    fun errorFromControllerAdded() {
        messages.collectFrom(source)
        onUi {} //подписка запускается на ui-потоке: дождаться её

        source.fail("boom")

        runBlocking { withTimeout(5000) { while (messages.list.isEmpty()) delay(10) } }
        assertEquals("boom", messages.list.single().text)
    }

    // remove убирает одно сообщение, clear — все
    @Test
    fun removeAndClear() {
        val a = GlobalMessage.createGlobalInfo("a", "a")
        val b = GlobalMessage.createGlobalInfo("b", "b")
        onUi {
            messages.add(a)
            messages.add(b)
            messages.remove(a.id)
        }
        assertEquals(listOf(b), messages.list)

        onUi { messages.clear() }
        assertEquals(emptyList(), messages.list)
    }
}