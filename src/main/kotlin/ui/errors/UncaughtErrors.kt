package non.era.aisdme.ui.errors

import non.era.aisdme.i18n.I18n
import non.era.aisdme.utils.Log
import javax.swing.SwingUtilities

private const val SAME_ERROR_LOG_MS = 5000L //одну и ту же ошибку в лог не чаще: отрисовка повторяет её каждый кадр

/**
 * Необработанные исключения потоков (события, корутины без обработчика) показываются в `ErrorOverlay`,
 * как остальные ошибки приложения, а не только в консоли. Ошибки кадра и событий окна Compose сюда не доходят:
 * их ловит сам Compose и показывает системным диалогом «Error» ([gotchas.md]).
 */
class UncaughtErrors(private val messages: GlobalMessages) : Thread.UncaughtExceptionHandler {
    private var lastText = ""
    private var lastLogged = 0L

    override fun uncaughtException(thread: Thread, e: Throwable) {
        runCatching {
            val text = "${e.message ?: I18n.s.unknownError}\n\n${e.stackTraceToString()}"
            if (text != lastText || System.currentTimeMillis() - lastLogged > SAME_ERROR_LOG_MS) {
                lastText = text
                lastLogged = System.currentTimeMillis()
                Log.e(e)
            }
            //состояние окна меняется на ui-потоке, но не посреди отрисовки, в которой исключение и случилось
            SwingUtilities.invokeLater { messages.addError(e::class.simpleName ?: I18n.s.error, text) }
        }
    }

    /** Ставит обработчик по умолчанию; результат — вернуть прежний */
    fun install(): AutoCloseable {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        return AutoCloseable { Thread.setDefaultUncaughtExceptionHandler(previous) }
    }
}
