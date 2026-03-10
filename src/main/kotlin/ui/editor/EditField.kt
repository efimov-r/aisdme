package non.era.aisdme.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import non.era.aisdme.configs.dataclass.ProjectConfig
import non.era.aisdme.di.LocalAppController
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.ResizableProporcialRow
import non.era.aisdme.ui.editor.halfview.texfield.TextEditorHalfContent
import non.era.aisdme.ui.editor.halfviewmodel.ObservableOpenedHalfFile

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
                EditorHalf(app.editorController.leftTabs, app.editorController.rightTabs,
                    modifier.fillMaxSize())
            },
            {
                EditorHalf(app.editorController.rightTabs, app.editorController.leftTabs,
                    modifier.fillMaxSize())
            })
    } else {
        EditorHalf(app.editorController.leftTabs, app.editorController.rightTabs, modifier.fillMaxSize())
    }
}

@Composable
fun EditorHalf(
    half: HalfEditorTabs,
    oppositeHalf: HalfEditorTabs,
    modifier: Modifier = Modifier
){
    val editor = LocalAppController.current.editorController
    val activeTab = half.activeTabId?.let { editor.getFile(it)}

    if(activeTab != null) {
        Column (
            modifier
                .then(
                    if (half.dragDest) Modifier.background(LocalAisDmeColors.current.dropTargetElement)
                    else Modifier
                )
                .onGloballyPositioned {
                    half.rect = it.boundsInWindow()
                }
                .fillMaxSize()
        ) {
            TabsBar(
                half = half,
                oppositeHalf = oppositeHalf,
                modifier = Modifier.height(44.dp).fillMaxWidth(),
            )
            Spacer(Modifier.height(1.dp).fillMaxWidth().background(LocalAisDmeColors.current.surfaceBorder))
            when(activeTab) {
                is ObservableOpenedHalfFile -> {
                    TextEditorHalfContent(
                        activeTab,
                        half.inFocus,
                        { half.inFocus = it.isFocused },
                    )
                }
                else -> {
                    Text(
                        text = "Error type half",
                        color = LocalAisDmeColors.current.onSurface,
                    )
                }
            }

        }
    } else {
        Box(modifier = modifier
            .then(
                if (half.dragDest) Modifier.background(LocalAisDmeColors.current.dropTargetElement)
                else Modifier
            ),
            contentAlignment = Alignment.Center) {
            Text(
                text = "Drop files here to open them",
                color = LocalAisDmeColors.current.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}