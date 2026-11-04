package non.era.aisdme.bus

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppEventBus {

    private val _events = MutableSharedFlow<AppEvent>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events = _events.asSharedFlow()

    fun publish(event: AppEvent) {
        _events.tryEmit(event)
    }
}