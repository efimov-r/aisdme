package non.era.aisdme.bus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import non.era.aisdme.utils.Log
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class AppEventBus {

    private val _events = MutableSharedFlow<AppEvent>(
        replay = 0, //подписчик, подключившийся позже, старых событий не получает
        extraBufferCapacity = Int.MAX_VALUE, //без потерь: события редкие, publish не ждёт
    )

    fun publish(event: AppEvent) {
        if(!_events.tryEmit(event)) Log.e("Событие потеряно: $event")
    }

    //UNDISPATCHED: подписка — до возврата, события сразу после subscribe не теряются
    fun subscribe(
        scope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        onEvent: suspend (AppEvent) -> Unit,
    ): Job = scope.launch(context, start = CoroutineStart.UNDISPATCHED) {
        _events.collect { onEvent(it) }
    }
}