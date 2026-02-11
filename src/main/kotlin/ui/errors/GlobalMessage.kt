package non.era.aisdme.ui.errors

import java.util.concurrent.atomic.AtomicInteger

enum class TypeGlobalMessage {
    INFO, WARNING, ERROR, QUESTION, SUCCESS
}

data class GlobalMessage(
    val id: Int,
    val type: TypeGlobalMessage,
    val title: String,
    val text: String
) {

    companion object {
        private var nextTabId = AtomicInteger(0)

        fun createGlobalInfo(title: String, err: String) = GlobalMessage(
                id = nextTabId.getAndIncrement(),
                type = TypeGlobalMessage.INFO,
                title = title,
                text = err
            )

        fun createGlobalQuestion(title: String, err: String) = GlobalMessage(
            id = nextTabId.getAndIncrement(),
            type = TypeGlobalMessage.QUESTION,
            title = title,
            text = err
        )

        fun createGlobalSuccess(title: String, err: String) = GlobalMessage(
            id = nextTabId.getAndIncrement(),
            type = TypeGlobalMessage.SUCCESS,
            title = title,
            text = err
        )

        fun createGlobalWarning(title: String, err: String) = GlobalMessage(
                id = nextTabId.getAndIncrement(),
                type = TypeGlobalMessage.WARNING,
                title = title,
                text = err
            )

        fun createGlobalError(title: String, err: String) = GlobalMessage(
                id = nextTabId.getAndIncrement(),
                type = TypeGlobalMessage.ERROR,
                title = title,
                text = err
            )
    }
}
