package non.era.aisdme.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.i18n.I18n
import non.era.aisdme.utils.Log
import non.era.aisdme.ui.errors.GlobalMessage

abstract class UiController {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val scopeFlow = CoroutineScope(SupervisorJob() + Dispatchers.Swing)
    private val _messages = MutableSharedFlow<GlobalMessage>( //ошибки, info, success — всё для ErrorOverlay
        replay = 0,
        extraBufferCapacity = 16
    )

    //UNDISPATCHED: подписка — до возврата, иначе сообщения до первого прохода ui-потока теряются (replay = 0)
    fun subscribeMessages(back: suspend (mess: GlobalMessage) -> Unit) =
        scopeFlow.launch(start = CoroutineStart.UNDISPATCHED) {
            _messages.collect { back(it) }
        }

    protected fun <T> safeLaunch(
        operation: suspend () -> T
    ): Job {
        return scope.launch {
            runCatching {
                operation()
            }.onFailure { e ->
                if (e is CancellationException) throw e //отмена — не ошибка
                withContext(Dispatchers.Swing) {
                    newGlobalError(e)
                }
            }
        }
    }

    protected suspend fun emitGlobalMessage(err: GlobalMessage) {
        _messages.emit(err)
    }

    protected suspend fun newGlobalInfo(title: String, text: String) {
        _messages.emit(GlobalMessage.createGlobalInfo(title, text))
    }

    protected suspend fun newGlobalQuestion(title: String, text: String) {
        _messages.emit(GlobalMessage.createGlobalQuestion(title, text))
    }

    protected suspend fun newGlobalSuccess(title: String, text: String) {
        _messages.emit(GlobalMessage.createGlobalSuccess(title, text))
    }

    protected suspend fun newGlobalWarning(title: String, text: String) {
        _messages.emit(GlobalMessage.createGlobalWarning(title, text))
    }

    protected suspend fun newGlobalError(err: String) {
        Log.e("Error: $err")
        _messages.emit(GlobalMessage.createGlobalError(I18n.s.error, err))
    }

    protected suspend fun newGlobalError(e: Throwable) {
        Log.e(e)

        val title = e::class.simpleName ?: I18n.s.error
        val message = e.message ?: I18n.s.unknownError
        val stackTrace = e.stackTraceToString()

        _messages.emit(GlobalMessage.createGlobalError(
            title = title,
            err = "$message\n\n$stackTrace"
        ))
    }

    open fun disposeBefore(){}
    open fun disposeAfter(){}

    fun dispose() {
        disposeBefore()
        scopeFlow.cancel()
        scope.cancel()
        disposeAfter()
    }
}