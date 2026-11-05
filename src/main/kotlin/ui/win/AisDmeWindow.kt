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
import non.era.aisdme.BuildConfig
import non.era.aisdme.configs.GlobalConfig
import non.era.aisdme.configs.dataclass.AppConfig
import java.io.File

sealed interface AppStartState {
    object Loading : AppStartState
    data class ChooseProject(val config: AppConfig) : AppStartState //последнего проекта нет
    data class Ready(val config: AppConfig, val lastProject: File) : AppStartState
}

@Composable
fun AisDmeWindow(onCloseRequest: () -> Unit,
        content: @Composable FrameWindowScope.(globalConf: AppConfig, project: File) -> Unit) {
    var startState by remember { mutableStateOf<AppStartState>(AppStartState.Loading) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        val config = GlobalConfig.store.get()
        var lastProject: File? = null

        if(config.lastOpenProject!= null) {
            val file = File(config.lastOpenProject)
            if(file.isDirectory) {
                lastProject = file
            }
        }
        val del = if(BuildConfig.DEBUG) 0 else 2000
        val elapsed = System.currentTimeMillis() - startTime
        val delayLeft = (del - elapsed).coerceAtLeast(0)

        delay(delayLeft)

        startState = lastProject?.let { AppStartState.Ready(config, it) }
            ?: AppStartState.ChooseProject(config)
    }

    // заставка одна на Loading и ChooseProject, чтобы окно не пересоздавалось.
    // без окон application завершается, поэтому заставка держится, пока открыт выбор
    val state = startState
    if (state is AppStartState.Ready) {
        MainWindow(state.config,
            onCloseRequest,
            state.lastProject,
            content)
    } else {
        SplashWindow(onCloseRequest) {
            if (state is AppStartState.ChooseProject) {
                LaunchedEffect(state) {
                    val dir = ProjectChooserHelper.chooseProjectDir(window)
                    if (dir == null) { //отмена — выходим
                        onCloseRequest()
                        return@LaunchedEffect
                    }
                    startState = AppStartState.Ready(state.config, dir)
                }
            }
        }
    }
}

@Composable
fun MainWindow(globalConf: AppConfig,
               onCloseRequest: () -> Unit,
               project: File,
               content: @Composable FrameWindowScope.(config: AppConfig, project: File) -> Unit) {
    Window(
        onCloseRequest = onCloseRequest,
        title = "AisDme",
        state = rememberPersistentWindowState(globalConf, GlobalConfig.store),
        icon = painterResource("logos/AisDme.png")
    ) {
        content(globalConf, project)
    }
}

@Composable
fun SplashWindow(onCloseRequest: () -> Unit, //закрыть заставку — выйти, иначе загрузка откроет главное окно
                 content: @Composable FrameWindowScope.() -> Unit = {}) {
    Window(
        onCloseRequest = onCloseRequest,
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
                    .clickable { onCloseRequest() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✕",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
        content()
    }
}