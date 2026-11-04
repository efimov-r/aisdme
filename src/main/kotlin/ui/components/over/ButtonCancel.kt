package non.era.aisdme.ui.components.over

import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.theme.aisDmeButtonBorder

@Composable
fun ButtonCancel(onClick: () -> Unit) =
    TextButton(
        modifier = Modifier.width(90.dp),
        shape = AisDmeShapes.medium,
        onClick = onClick,
        border = aisDmeButtonBorder(),
        colors = ButtonDefaults.textButtonColors(contentColor = LocalAisDmeColors.current.onSurface)
    ) {
        Text("Cancel")
    }