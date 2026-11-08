package non.era.aisdme.ui.errors

import non.era.aisdme.utils.Log
import javax.swing.SwingUtilities

private const val SAME_ERROR_LOG_MS = 5000L //одну и ту же ошибку в лог не чаще: отрисовка повторяет её каждый кадр

/**
 * Необработанные исключения любого потока и окна Compose (отрисовка, события) показываются в `ErrorOverlay`,
 * как остальные ошибки приложения: без системного диалога и без закрытия окна ([WindowErrors]).
 */
class UncaughtErrors(private val messages: GlobalMessages) : Thread.UncaughtExceptionHandler {
    private var lastText = ""
    private var lastLogged = 0L

    override fun uncaughtException(thread: Thread, e: Throwable) {
        runCatching {
            val text = "${e.message ?: "Unknown error"}\n\n${e.stackTraceToString()}"
            if (text != lastText || System.currentTimeMillis() - lastLogged > SAME_ERROR_LOG_MS) {
                lastText = text
                lastLogged = System.currentTimeMillis()
                Log.e(e)
            }
            //состояние окна меняется на ui-потоке, но не посреди отрисовки, в которой исключение и случилось
            SwingUtilities.invokeLater { messages.addError(e::class.simpleName ?: "Error", text) }
        }
    }

    /** Ставит обработчик по умолчанию и для окон Compose; результат — вернуть прежние */
    fun install(): AutoCloseable {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        WindowErrors.target = this
        return AutoCloseable {
            Thread.setDefaultUncaughtExceptionHandler(previous)
            WindowErrors.target = null
        }
    }
}
