package non.era.aisdme.ui.errors

import java.util.concurrent.atomic.AtomicInteger

data class ErrorData(
    val id: Int,
    val text: String
) {

    companion object {
        private var nextTabId = AtomicInteger(0)

        fun createErrorObj(err: String): ErrorData{
            return ErrorData(
                id = nextTabId.getAndIncrement(),
                text = err
            )
        }
    }
}
