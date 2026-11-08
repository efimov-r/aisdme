package non.era.aisdme.ui.errors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UncaughtErrorsTest {
    private val messages = GlobalMessages()
    private val handler = UncaughtErrors(messages)

    private fun waitForMessages(count: Int) =
        runBlocking { withTimeout(5000) { while (messages.list.size < count) delay(10) } }

    private fun onUi(block: () -> Unit) = runBlocking(Dispatchers.Swing) { block() }

    // Необработанное исключение любого потока попадает в список сообщений как ошибка с типом и текстом
    @Test
    fun uncaughtExceptionBecomesErrorMessage() {
        handler.uncaughtException(Thread.currentThread(), IllegalStateException("boom"))
        waitForMessages(1)

        val mess = messages.list.single()
        assertEquals(TypeGlobalMessage.ERROR, mess.type)
        assertEquals("IllegalStateException", mess.title)
        assertTrue(mess.text.startsWith("boom"))
    }

    // То же исключение, пока ошибка на экране, второй раз не добавляется (отрисовка повторяет его каждый кадр)
    @Test
    fun sameErrorIsNotAddedTwice() {
        val e = IllegalStateException("boom")
        handler.uncaughtException(Thread.currentThread(), e)
        waitForMessages(1)
        handler.uncaughtException(Thread.currentThread(), e)
        onUi { } //очередь ui-потока отработала

        assertEquals(1, messages.list.size)
    }

    // Другая ошибка добавляется рядом; закрытую можно показать снова
    @Test
    fun differentErrorAndClosedErrorAreAdded() {
        onUi {
            messages.addError("A", "text")
            messages.addError("B", "text")
        }
        assertEquals(2, messages.list.size)

        onUi {
            messages.remove(messages.list.first().id)
            messages.addError("A", "text")
        }
        assertEquals(2, messages.list.size)
    }

    // install ставит обработчик по умолчанию, close возвращает прежний
    @Test
    fun installSetsAndRestoresDefaultHandler() {
        val before = Thread.getDefaultUncaughtExceptionHandler()
        val installed = handler.install()
        assertEquals(handler, Thread.getDefaultUncaughtExceptionHandler())

        installed.close()
        assertEquals(before, Thread.getDefaultUncaughtExceptionHandler())
    }
}
