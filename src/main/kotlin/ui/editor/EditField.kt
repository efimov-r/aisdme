package non.era.aisdme.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.LocalAppController
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.ui.components.custom.ResizableProporcialRow
import non.era.aisdme.ui.components.custom.ScrollableContent
import kotlin.text.toFloat

@Composable
fun Editor(conf: ProjectConfig?, modifier: Modifier = Modifier) {
    val app = LocalAppController.current
    val leftRiteConfig = "ResizableProporcialRowEditorLeftAndRight"

    if(app.getEditorHalfLeftController().countTabs() == 0 && app.getEditorHalfRightController().countTabs() > 0){
        app.rightToLeftHalf()
    }

    if(app.getEditorHalfLeftController().countTabs() > 0 && app.getEditorHalfRightController().countTabs() == 0){
        EditorHalf(app.getEditorHalfLeftController(), modifier.fillMaxSize())
    } else if(app.getEditorHalfLeftController().countTabs() > 0 && app.getEditorHalfRightController().countTabs() > 0) {
        ResizableProporcialRow(Modifier.fillMaxSize(),
            conf?.keyValue?.get(leftRiteConfig)?.toFloat() ?: 0.5f,
            { app.updateKeyValueProjectConfig(leftRiteConfig, it.toString()) },
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
            tabs = listTab.getTabs(),
            activeId = listTab.getActiveTabId(),
            onSelect = { listTab.activateTab(it) },
            onClose = { listTab.closeTab(it) }
        )

        listTab.getActiveTab()?.let { tab ->
            EditField(
                text = tab.getText(),
                error = tab.error,
                readOnly = tab.readOnly,
                loadState = tab.loadState,
                onTextChange = { listTab.updateText(it) }
            )
        }
    }
}

@Composable
fun EditField(
    text: String,
    error: String?,
    readOnly: Boolean,
    loadState: Boolean,
    onTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (error != null) {
            Text(
                text = "Error: $error",
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (loadState) {
                    Text(
                        text = "Load ... ",
                        color = colors.onBackground,
                        fontSize = 14.sp
                    )
                } else {
                    ScrollableContent { scrollState->
                        BasicTextField(
                            value = text,
                            readOnly = readOnly,
                            onValueChange = onTextChange,
                            textStyle = TextStyle(
                                color = colors.onSurface,
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
    }
}