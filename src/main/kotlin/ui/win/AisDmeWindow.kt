package non.era.aisdme.ui.win

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import java.awt.Frame

@Composable
fun AisDmeWindow(onCloseRequest: () -> Unit,
                 content: @Composable FrameWindowScope.() -> Unit) =
    Window(
        onCloseRequest = onCloseRequest,
        title = "AisDme",
        undecorated = false,
        resizable = true
    ) {
        window.extendedState = Frame.MAXIMIZED_BOTH
        content()
    }