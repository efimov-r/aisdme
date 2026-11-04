package non.era.aisdme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import non.era.aisdme.configs.GlobalConfig
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.di.buildAppController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.AisDmeSpacing
import non.era.aisdme.theme.AisDmeTheme
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.ResizableColumn
import non.era.aisdme.ui.components.custom.ResizableRow
import non.era.aisdme.ui.components.dialogs.elements.UiDialog
import non.era.aisdme.ui.components.dialogs.elements.DialogsStarter
import non.era.aisdme.ui.components.dialogs.elements.TooltipStarter
import non.era.aisdme.ui.editor.Editor
import non.era.aisdme.ui.errors.view.ErrorOverlay
import non.era.aisdme.ui.move.DragPreviewLayer
import non.era.aisdme.ui.project.view.ProjectTreeView
import non.era.aisdme.ui.win.AisDmeWindow
import non.era.aisdme.utils.Log

fun main() = application {
    if(BuildConfig.DEBUG) {
        Log.d("Debug!!!!")
    }

    AisDmeWindow(onCloseRequest = ::exitApplication) { globalConf, project->
        val appController = buildAppController(globalConf)

        val configProjectStart = remember {
            appController.reselectProject(project)
        }

        var activeDialog by remember { mutableStateOf<UiDialog?>(null) }
        LaunchedEffect(Unit) { appController.dialogController.effects.collect { activeDialog = it } }

        DisposableEffect(Unit) {
            onDispose {
                Log.d("DisposableEffect do")
                appController.dispose()
            }
        }

        CompositionLocalProvider(
            LocalAppController provides appController
        ) {
            AisDmeTheme(appController.isDarkTheme) {
                AisDmeContent(configProjectStart)
                DialogsStarter(activeDialog) { activeDialog = null }
                TooltipStarter(appController.dialogController.tolltipState)
            }
        }
    }

    Log.d("the end of main")
}

@Composable
fun AisDmeContent(configProjectStart: ProjectConfig?) {
    val colName = "ResizableColumnMainTerminalAndOther"
    val rowName = "ResizableRowMainTreeAndEditor"
    val app = LocalAppController.current
    Row(Modifier.fillMaxSize().background(LocalAisDmeColors.current.background).padding(AisDmeSpacing.s)) {
        ResizableColumn(Modifier.fillMaxHeight().weight(1f),
             configProjectStart?.keyValue?.get(colName)?.toFloat() ?: 250f,
            { app.configController.updateKeyValue(colName, it.toString()) },
            {
                ResizableRow(Modifier.fillMaxSize(),
                    configProjectStart?.keyValue?.get(rowName)?.toFloat() ?: 250f,
                    { app.configController.updateKeyValue(rowName, it.toString()) },
                    {
                        ProjectTreeView(Modifier.fillMaxSize().panel())
                    },
                    {
                        Editor(configProjectStart, Modifier.panel())
                    })
            },
            {
                Box(Modifier.fillMaxSize().panel())
            })
        Box(Modifier.fillMaxHeight().width(60.dp).padding(start = 6.dp).panel().padding(AisDmeSpacing.s)) {
            val scope = rememberCoroutineScope()
            IconButton(onClick = {
                val new = !app.isDarkTheme
                app.isDarkTheme = new
                scope.launch {
                    GlobalConfig.store.update { it.copy(isDarkTheme = new) }
                }
            }) {
                Icon(
                    imageVector = if (app.isDarkTheme)
                        Icons.Default.Email
                    else
                        Icons.Default.DateRange,
                    contentDescription = "Toggle theme"
                )
            }
        }
    }
    ErrorOverlay()
    DragPreviewLayer()
}

@Composable
fun Modifier.panel(): Modifier {
    return clip(AisDmeShapes.large).background(LocalAisDmeColors.current.surface)
}