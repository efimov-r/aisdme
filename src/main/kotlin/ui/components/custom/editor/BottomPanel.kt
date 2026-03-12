package non.era.aisdme.ui.components.custom.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.editor.halfviewmodel.TabCashFile
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.utils.PathHelper
import java.nio.file.Path

@Composable
fun BottomPanel(tab: TabCashFile) {
    Row(modifier = Modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(4.dp))
        Path(tab.path)
        Spacer(Modifier.weight(1f))
        WriteStatus(tab.writeStatus)
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun Path(path: Path?) {
    Box(modifier = Modifier.height(24.dp)
        .clip(AisDmeShapes.large)
        .background(LocalAisDmeColors.current.surface)
        .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = path?.let{ PathHelper.pathToStrFormat(it)} ?: "",
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