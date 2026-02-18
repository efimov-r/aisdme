package non.era.aisdme.ui.win

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import non.era.aisdme.configs.GlobalConfig
import non.era.aisdme.configs.dataclass.AppConfig

sealed interface AppStartState {
    object Loading : AppStartState
    data class Ready(val config: AppConfig) : AppStartState
}

@Composable
fun AisDmeWindow(onCloseRequest: () -> Unit,
        content: @Composable FrameWindowScope.() -> Unit) {
    var startState by remember { mutableStateOf<AppStartState>(AppStartState.Loading) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        val config = GlobalConfig.store.get()

        val elapsed = System.currentTimeMillis() - startTime
        val delayLeft = (1000 - elapsed).coerceAtLeast(0)

        delay(delayLeft)

        startState = AppStartState.Ready(config)
    }

    when (val state = startState) {
        is AppStartState.Loading -> SplashWindow()
        is AppStartState.Ready -> MainWindow(state.config,
            onCloseRequest,
            content)
    }
}

@Composable
fun MainWindow(config: AppConfig,
               onCloseRequest: () -> Unit,
               content: @Composable FrameWindowScope.() -> Unit) {
    Window(
        onCloseRequest = onCloseRequest,
        state = rememberPersistentWindowState(config, GlobalConfig.store)
    ) {
        content()
    }
}

@Composable
fun SplashWindow() {
    Window(
        onCloseRequest = {},
        title = "Loading",
        state = rememberWindowState(
            size = DpSize(300.dp, 200.dp),
            position = WindowPosition(Alignment.Center)
        ),
        resizable = false
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading config...")
        }
    }
}