package non.era.aisdme.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.LocalAppController
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.ResizableProporcialRow
import non.era.aisdme.ui.components.custom.ScrollableContent

@Composable
fun Editor(conf: ProjectConfig?, modifier: Modifier = Modifier) {
    val app = LocalAppController.current
    val leftRightConfig = "ResizableProporcialRowEditorLeftAndRight"

    if(app.getEditorHalfLeftController().countTabs() == 0 && app.getEditorHalfRightController().countTabs() > 0){
        app.rightToLeftHalf()
    }

    if(app.getEditorHalfLeftController().countTabs() > 0 && app.getEditorHalfRightController().countTabs() == 0){
        EditorHalf(app.getEditorHalfLeftController(), modifier.fillMaxSize())
    } else if(app.getEditorHalfLeftController().countTabs() > 0 && app.getEditorHalfRightController().countTabs() > 0) {
        ResizableProporcialRow(Modifier.fillMaxSize(),
            conf?.keyValue?.get(leftRightConfig)?.toFloat() ?: 0.5f,
            { app.updateKeyValueProjectConfig(leftRightConfig, it.toString()) },
            {
                EditorHalf(app.getEditorHalfLeftController(),
                    modifier.fillMaxSize())
            },
            {
                EditorHalf(app.getEditorHalfRightController(),
                    modifier.fillMaxSize())
            })
    } else { //пусто
        Box(modifier.fillMaxSize())
    }
}

@Composable
fun EditorHalf(listTab: EditorHalfState, modifier: Modifier = Modifier){
    Column (modifier) {
        TabsBar(
            modifier = Modifier.height(44.dp).fillMaxWidth(),
            tabs = listTab.getTabs(),
            activeId = listTab.getActiveTabId(),
            onSelect = { listTab.activateTab(it) },
            onClose = { listTab.closeTab(it) }
        )
        Spacer(Modifier.height(1.dp).fillMaxWidth().background(LocalAisDmeColors.current.surfaceBorder))
        listTab.getActiveTab()?.let { tab ->
            EditField(
                text = tab.text,
                path = tab.path,
                writeStatus = tab.writeStatus,
                onTextChange = { listTab.updateText(it) }
            )
        }
    }
}

@Composable
fun EditField(
    text: String,
    path: String,
    writeStatus: WriteStatus,
    onTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.weight(1f)) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (writeStatus == WriteStatus.LOAD) {
                    Text(
                        text = "Load ... ",
                        color = LocalAisDmeColors.current.onBackground,
                        fontSize = 14.sp
                    )
                } else {
                    ScrollableContent { scrollState->
                        BasicTextField(
                            value = text,
                            readOnly = (writeStatus == WriteStatus.READ_ONLY_FS),
                            onValueChange = onTextChange,
                            textStyle = TextStyle(
                                color = LocalAisDmeColors.current.onSurface,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        )
                    }
                }
            }

        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(LocalAisDmeColors.current.surfaceBorder))
        BottomPanel(path, writeStatus)
    }
}

@Composable
private fun BottomPanel(path: String, writeStatus: WriteStatus,) {
    Row(modifier = Modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(4.dp))
        Path(path)
        Spacer(Modifier.weight(1f))
        WriteStatus(writeStatus)
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun Path(path: String) {
    Box(modifier = Modifier.height(24.dp)
        .clip(AisDmeShapes.large)
        .background(LocalAisDmeColors.current.surface)
        .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = path,
            modifier = Modifier.padding(bottom = 1.dp).padding(horizontal = 16.dp),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            color = LocalAisDmeColors.current.onSurface
        )
    }
}

@Composable
private fun WriteStatus(writeStatus: WriteStatus) {
    Box(modifier = Modifier.width(76.dp).height(24.dp)
        .clip(AisDmeShapes.large)
        .background(writeStatus.back)
        .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = writeStatus.text,
            modifier = Modifier.padding(bottom = 1.dp),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            color = writeStatus.te
        )
    }
}