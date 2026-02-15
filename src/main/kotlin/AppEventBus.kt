package non.era.aisdme

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppEventBus {

    private val _events = MutableSharedFlow<AppEvent>(
        extraBufferCapacity = 64
    )

    val events = _events.asSharedFlow()

    fun publish(event: AppEvent) {
        _events.tryEmit(event)
    }
}