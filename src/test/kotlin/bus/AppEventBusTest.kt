package non.era.aisdme.bus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals

class AppEventBusTest {
    private val bus = AppEventBus()
    private val scope = CoroutineScope(Dispatchers.Swing)
    private val received = CopyOnWriteArrayList<AppEvent>()

    @AfterEach
    fun dispose() = scope.cancel()

    private fun event(i: Int) = ShowFileInTree(File("$i"))

    private fun onUi(block: () -> Unit) = runBlocking(Dispatchers.Swing) { block() }

    private fun waitSize(size: Int) = runBlocking {
        withTimeout(5000) {
            while (received.size < size) delay(10)
        }
    }

    // подписчик, подключившийся после события, его не получает
    @Test
    fun lateSubscriberGetsNoOldEvent() {
        bus.publish(event(0))

        bus.subscribe(scope) { received += it }
        bus.publish(event(1))

        waitSize(1)
        assertEquals(listOf<AppEvent>(event(1)), received.toList())
    }

    // 1000 событий, пока подписчик занят, — доходят все
    @Test
    fun burstWhileSubscriberBusyNotLost() {
        bus.subscribe(scope) { received += it }

        onUi { //подписчик на ui-потоке не может забирать события
            repeat(1000) { bus.publish(event(it)) }
        }

        waitSize(1000)
        assertEquals((0 until 1000).map { event(it) }, received.toList())
    }

    // событие сразу после subscribe доходит, даже если поток подписчика занят
    @Test
    fun eventRightAfterSubscribeNotLost() {
        onUi {
            bus.subscribe(scope) { received += it }
            bus.publish(event(0))
        }

        waitSize(1)
        assertEquals(listOf<AppEvent>(event(0)), received.toList())
    }
}