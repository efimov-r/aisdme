package non.era.aisdme.ui.errors

import non.era.aisdme.utils.Log
import javax.swing.SwingUtilities

/**
 * Необработанные исключения любого потока (отрисовка, корутины без обработчика) показываются в `ErrorOverlay`,
 * как остальные ошибки приложения, а не только в консоли запуска.
 */
class UncaughtErrors(private val messages: GlobalMessages) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, e: Throwable) {
        runCatching {
            Log.e(e)
            //состояние окна меняется на ui-потоке, но не посреди отрисовки, в которой исключение и случилось
            val text = "${e.message ?: "Unknown error"}\n\n${e.stackTraceToString()}"
            SwingUtilities.invokeLater { messages.addError(e::class.simpleName ?: "Error", text) }
        }
    }

    /** Ставит обработчик по умолчанию; результат — вернуть прежний */
    fun install(): AutoCloseable {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        return AutoCloseable { Thread.setDefaultUncaughtExceptionHandler(previous) }
    }
}
