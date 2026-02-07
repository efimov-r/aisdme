package non.era.aisdme.ui.editor

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.LocalAppController
import non.era.aisdme.theme.AisDmeSpacing

@Composable
fun Editor(modifier: Modifier = Modifier) {
    val app = LocalAppController.current

    if(app.getEditorHalfLeftController().countTabs() == 0 && app.getEditorHalfRightController().countTabs() > 0){
        app.rightToLeftHalf()
    }

    if(app.getEditorHalfLeftController().countTabs() > 0 && app.getEditorHalfRightController().countTabs() == 0){
        EditorHalf(app.getEditorHalfLeftController(), modifier.fillMaxSize())
    } else if(app.getEditorHalfLeftController().countTabs() > 0 && app.getEditorHalfRightController().countTabs() > 0) {
        Row(Modifier.fillMaxSize()) {
            EditorHalf(app.getEditorHalfLeftController(),
                modifier.fillMaxHeight().weight(1f))
            Box(Modifier.fillMaxHeight().width(AisDmeSpacing.s))
            EditorHalf(app.getEditorHalfRightController(),
                modifier.fillMaxHeight().weight(1f))
        }
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
                if (text.isEmpty()) {
                    Text(
                        text = ";; source\\n;; Type your code here",
                        color = colors.onBackground,
                        fontSize = 14.sp
                    )
                } else {
                    val scrollState = rememberScrollState()

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

                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(scrollState)
                    )
                }
            }
        }
    }
}