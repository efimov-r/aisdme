package non.era.aisdme.ui.editor.halfview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.custom.editor.BottomPanel
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile

@Composable
fun HalfContentFile(
    tab: TabCashFile,
    content: @Composable BoxScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 1.dp)
            ) {
                content()
            }
        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(LocalAisDmeColors.current.surfaceBorder))
        BottomPanel(tab)
    }
}