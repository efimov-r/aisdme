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
import non.era.aisdme.ui.editor.halfview.FromClipboardHalfContent
import non.era.aisdme.ui.editor.halfview.HalfContentFile
import non.era.aisdme.ui.editor.halfview.HalfContentFileImg
import non.era.aisdme.ui.editor.halfview.HalfContentFileText
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileImg
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashPaste

@Composable
fun Editor(conf: ProjectConfig?, modifier: Modifier = Modifier) {
    val app = LocalAppController.current
    val leftRightConfig = "ResizableProporcialRowEditorLeftAndRight"
    val left = app.editorController.leftTabs
    val right = app.editorController.rightTabs

    if(left.listTabs.size > 0 && right.listTabs.size > 0) {
        ResizableProporcialRow(Modifier.fillMaxSize(),
            conf?.keyValue?.get(leftRightConfig)?.toFloat() ?: 0.5f,
            { app.configController.updateKeyValue(leftRightConfig, it.toString()) },
            {
                EditorHalf(left, right, modifier.fillMaxSize())
            },
            {
                EditorHalf(right, left, modifier.fillMaxSize())
            })
    } else {
        val onlyRight = left.listTabs.isEmpty() && right.listTabs.isNotEmpty() //пока правая не перенесена в левую
        EditorHalf(if(onlyRight) right else left, if(onlyRight) left else right, modifier.fillMaxSize())
    }
}

@Composable
fun EditorHalf(
    half: HalfEditorTabs,
    oppositeHalf: HalfEditorTabs,
    modifier: Modifier = Modifier
){
    val editor = LocalAppController.current.editorController
    val activeTab = half.activeTabId?.let { editor.getTab(it)}

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
                is TabCashFileText -> {
                    HalfContentFile(activeTab) {
                        HalfContentFileText(
                            activeTab,
                            half.inFocus,
                            { half.inFocus = it.isFocused },
                        )
                    }
                }
                is TabCashPaste -> {
                    FromClipboardHalfContent(
                        activeTab,
                        half.inFocus,
                        { half.inFocus = it.isFocused },
                    )
                }
                is TabCashFileImg -> {
                    HalfContentFile(activeTab) {
                        HalfContentFileImg(
                            activeTab,
                            half.inFocus,
                            { half.inFocus = it },
                        )
                    }
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