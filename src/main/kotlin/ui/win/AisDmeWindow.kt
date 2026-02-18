package non.era.aisdme.ui.win

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        val delayLeft = (2000 - elapsed).coerceAtLeast(0)

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
        title = "AisDme",
        state = rememberPersistentWindowState(config, GlobalConfig.store),
        icon = painterResource("logos/AisDme.png")
    ) {
        content()
    }
}

@Composable
fun SplashWindow() {
    var isOpen by remember { mutableStateOf(true) }

    if(isOpen) {
        Window(
            onCloseRequest = { isOpen = false },
            title = "Loading",
            undecorated = true,          // убираем рамку ОС
            transparent = false,         // true только если нужна альфа
            resizable = false,
            state = rememberWindowState(
                size = DpSize(500.dp, 500.dp),
                position = WindowPosition(Alignment.Center)
            )
        ) {
            Box {
                Image(
                    painter = painterResource("logos/AisDme_big.png"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(all = 8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000))
                        .clickable { isOpen = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✕",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}