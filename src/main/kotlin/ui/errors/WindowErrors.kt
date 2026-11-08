package non.era.aisdme.ui.errors

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.DefaultWindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import java.awt.Window

/**
 * Обработчик исключений окна Compose (отрисовка кадра, события): вместо стандартного системного диалога «Error»
 * с закрытием окна отдаёт ошибку в [UncaughtErrors]. Пока он не подключён (окно загрузки), работает стандартный.
 */
@OptIn(ExperimentalComposeUiApi::class)
object WindowErrors : WindowExceptionHandlerFactory {
    @Volatile
    var target: UncaughtErrors? = null

    override fun exceptionHandler(window: Window) = WindowExceptionHandler { e ->
        val handler = target
        if (handler == null) DefaultWindowExceptionHandlerFactory.exceptionHandler(window).onException(e)
        else handler.uncaughtException(Thread.currentThread(), e)
    }
}
