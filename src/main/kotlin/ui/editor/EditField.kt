package non.era.aisdme.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.ResizableProporcialRow
import non.era.aisdme.ui.components.custom.ScrollableContent

@Composable
fun Editor(conf: ProjectConfig?, modifier: Modifier = Modifier) {
    val app = LocalAppController.current
    val leftRightConfig = "ResizableProporcialRowEditorLeftAndRight"

    if(app.editorController.leftTabs.listTabs.size == 0 && app.editorController.rightTabs.listTabs.size > 0){
        app.editorController.rightToLeftHalf()
    }

    if(app.editorController.leftTabs.listTabs.size > 0 && app.editorController.rightTabs.listTabs.size > 0) {
        ResizableProporcialRow(Modifier.fillMaxSize(),
            conf?.keyValue?.get(leftRightConfig)?.toFloat() ?: 0.5f,
            { app.configController.updateKeyValue(leftRightConfig, it.toString()) },
            {
                EditorHalf(app.editorController.leftTabs,
                    modifier.fillMaxSize())
            },
            {
                EditorHalf(app.editorController.rightTabs,
                    modifier.fillMaxSize())
            })
    } else {
        EditorHalf(app.editorController.leftTabs, modifier.fillMaxSize())
    }
}

@Composable
fun EditorHalf(half: HalfEditorTabs, modifier: Modifier = Modifier){
    val editor = LocalAppController.current.editorController
    val activeTab by remember {
        derivedStateOf {
            half.activeTabId?.let { editor.getFile(it)}
        }
    }

    val intMod = if(half.dragDest) {
        modifier.background(LocalAisDmeColors.current.dropTargetElement)
    } else {
        modifier
    }

    Column (
        intMod
            .onGloballyPositioned {
                half.rect = it.boundsInWindow()
            }
    ) {
        if(half.listTabs.size > 0) {
            TabsBar(
                half = half,
                modifier = Modifier.height(44.dp).fillMaxWidth(),
            )
            Spacer(Modifier.height(1.dp).fillMaxWidth().background(LocalAisDmeColors.current.surfaceBorder))
        }
        activeTab?.let { tab ->
            EditField(
                text = tab.text,
                path = tab.path,
                writeStatus = tab.writeStatus,
                onTextChange = { newText->
                    half.activeTabId?.let{ editor.updateTextAndAutosafeFile(it, newText) }
                },
                half = half,
            )
        } ?: run {
            if(half.listTabs.size < 1) {
                Box(modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    Text(
                        text = "Drop files here to open them",
                        color = LocalAisDmeColors.current.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
fun EditField(
    text: String,
    path: String,
    writeStatus: WriteStatus,
    onTextChange: (String) -> Unit,
    half: HalfEditorTabs,
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
                    val focusRequester = remember { FocusRequester() }

                    LaunchedEffect(half.inFocus) {
                        if (half.inFocus) {
                            focusRequester.requestFocus()
                        }
                    }

                    ScrollableContent { scrollState->
                        BasicTextField(
                            value = text,
                            readOnly = writeStatus.readOnly,
                            onValueChange = onTextChange,
                            textStyle = TextStyle(
                                color = LocalAisDmeColors.current.onSurface,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .focusRequester(focusRequester)
                                .onFocusChanged {
                                    half.inFocus = it.isFocused
                                }
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