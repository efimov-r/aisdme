package non.era.aisdme.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.utils.Log
import non.era.aisdme.ui.errors.GlobalMessage

abstract class UiController {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val scopeFlow = CoroutineScope(SupervisorJob() + Dispatchers.Swing)
    private val _errors = MutableSharedFlow<GlobalMessage>(
        replay = 0,
        extraBufferCapacity = 16
    )

    //execute in init with oll internal Controllers
    fun subscriptionError(back: suspend (error: GlobalMessage) -> Unit) =
        scopeFlow.launch {
            _errors.collect { back(it) }
            }

    protected fun <T> safeLaunch(
        operation: suspend () -> T
    ): Job {
        return scope.launch {
            runCatching {
                operation()
            }.onFailure { e ->
                withContext(Dispatchers.Swing) {
                    newGlobalError(e)
                }
            }
        }
    }

    protected suspend fun emitGlobalMessage(err: GlobalMessage) {
        _errors.emit(err)
    }

    protected suspend fun newGlobalInfo(title: String, text: String) {
        _errors.emit(GlobalMessage.createGlobalInfo(title, text))
    }

    protected suspend fun newGlobalQuestion(title: String, text: String) {
        _errors.emit(GlobalMessage.createGlobalQuestion(title, text))
    }

    protected suspend fun newGlobalSuccess(title: String, text: String) {
        _errors.emit(GlobalMessage.createGlobalSuccess(title, text))
    }

    protected suspend fun newGlobalWarning(title: String, text: String) {
        _errors.emit(GlobalMessage.createGlobalWarning(title, text))
    }

    protected suspend fun newGlobalError(err: String) {
        Log.e("Error: $err")
        _errors.emit(GlobalMessage.createGlobalError("Error", err))
    }

    protected suspend fun newGlobalError(e: Throwable) {
        Log.e(e)

        val title = e::class.simpleName ?: "Error"
        val message = e.message ?: "Unknown error"
        val stackTrace = e.stackTraceToString()

        _errors.emit(GlobalMessage.createGlobalError(
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