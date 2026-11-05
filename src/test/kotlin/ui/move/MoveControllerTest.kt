package non.era.aisdme.ui.move

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import kotlin.test.Test

class MoveControllerTest {
    private val controller = MoveController()

    @AfterEach
    fun dispose() = controller.dispose()

    // у края списка, который некуда прокручивать, автоскролл останавливается сам (#3)
    @Test
    fun autoScrollStopsAtEndOfList() {
        controller.setScrollState(LazyListState())
        controller.setListBounds(Rect(0f, 0f, 100f, 300f))
        controller.onDragStart(Offset(50f, 150f), listOf())

        controller.setDragPosition(Offset(50f, 290f))

        runBlocking {
            withTimeout(5000) {
                while (controller.isAutoScrolling()) delay(10)
            }
        }
    }
}