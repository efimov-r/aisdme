package non.era.aisdme

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.fail

/** Ждёт условие, которое выполняет фоновая корутина или поток; не дождался — тест падает */
fun awaitUntil(timeoutMs: Long = 5000, what: String = "condition", condition: () -> Boolean) {
    val done = runBlocking {
        withTimeoutOrNull(timeoutMs) {
            while (!condition()) delay(10)
            true
        }
    }
    if (done != true) fail("Timeout waiting for $what")
}