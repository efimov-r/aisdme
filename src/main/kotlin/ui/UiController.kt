package non.era.aisdme.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.swing.Swing
import non.era.aisdme.IControler
import non.era.aisdme.Log
import non.era.aisdme.ui.errors.ErrorData

abstract class UiController : IControler {
    protected val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val scopeFlow = CoroutineScope(SupervisorJob() + Dispatchers.Swing)
    private val _errors = MutableSharedFlow<ErrorData>(
        replay = 0,
        extraBufferCapacity = 16
    )

    //execute in init with oll internal Controllers
    fun subscriptionError(back: suspend (error: ErrorData) -> Unit) =
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
                    newError(e)
                }
            }
        }
    }

    protected suspend fun newError(err: ErrorData) {
        _errors.emit(err)
    }

    protected suspend fun newError(err: String) {
        Log.e("Error: $err")
        _errors.emit(ErrorData.createErrorObj(err))
    }

    protected suspend fun newError(e: Throwable) {
        Log.e("Error ${e.message}")
        _errors.emit(ErrorData.createErrorObj(e.message ?: "Unknown error"))
    }

    open fun disposeBefore(){}
    open fun disposeAfter(){}

    final override fun dispose() {
        disposeBefore()
        scopeFlow.cancel()
        scope.cancel()
        disposeAfter()
    }
}