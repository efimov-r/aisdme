package non.era.aisdme.ui.components.dialogs.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.contexmenu.SmartMenuPositionProvider

@Composable
fun TooltipStarter(state: TolltipState) {
    if(state.show){
        Popup(
            popupPositionProvider = SmartMenuPositionProvider(state.position, 40),
            onDismissRequest = {},
            focusable = false
        ) {
            Box(Modifier
                .clip(AisDmeShapes.small)
                .background(LocalAisDmeColors.current.surface)
                .border(1.dp, LocalAisDmeColors.current.surfaceBorder, AisDmeShapes.small)) {
                Text(
                    text = state.text,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 4.dp, bottom = 6.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}