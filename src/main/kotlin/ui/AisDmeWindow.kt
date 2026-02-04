package non.era.aisdme.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window

@Composable
fun AisDmeWindow(onCloseRequest: () -> Unit,
                 content: @Composable FrameWindowScope.() -> Unit) =
    Window(
        onCloseRequest = onCloseRequest,
        title = "AisDme",
        undecorated = false,
        resizable = true
    ) {
        window.extendedState = java.awt.Frame.MAXIMIZED_BOTH
        content()
    }