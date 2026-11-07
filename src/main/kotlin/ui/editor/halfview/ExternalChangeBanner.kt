package non.era.aisdme.ui.editor.halfview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.theme.aisDmeButtonBorder
import non.era.aisdme.ui.editor.halfviewmodel.WriteStatus
import non.era.aisdme.ui.editor.halfviewmodel.realizations.ExternalChoice
import non.era.aisdme.ui.editor.halfviewmodel.realizations.TabCashFileText

/** Плашка над текстом: файл изменили или удалили снаружи, а в вкладке есть несохранённый ввод */
@Composable
fun ExternalChangeBanner(
    file: TabCashFileText,
    onCloseTab: () -> Unit,
) {
    val colors = LocalAisDmeColors.current
    val changed = file.writeStatus == WriteStatus.CONFLICT

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp).background(colors.surface).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (changed) "File changed on disk" else "File deleted from disk",
                color = colors.onSurface,
                fontSize = 12.sp,
            )
            Spacer(Modifier.weight(1f))
            if (changed) {
                BannerButton("Load from disk") { file.resolve(ExternalChoice.LOAD_FROM_DISK) }
                Spacer(Modifier.width(8.dp))
                BannerButton("Keep mine") { file.resolve(ExternalChoice.KEEP_MINE) }
            } else {
                BannerButton("Save again") { file.resolve(ExternalChoice.SAVE_AGAIN) }
                Spacer(Modifier.width(8.dp))
                BannerButton("Close tab", onCloseTab)
            }
        }
        Spacer(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceBorder))
    }
}

@Composable
private fun BannerButton(text: String, onClick: () -> Unit) {
    TextButton(
        shape = AisDmeShapes.medium,
        onClick = onClick,
        border = aisDmeButtonBorder(),
        colors = ButtonDefaults.textButtonColors(contentColor = LocalAisDmeColors.current.onSurface),
    ) {
        Text(text, fontSize = 12.sp)
    }
}
