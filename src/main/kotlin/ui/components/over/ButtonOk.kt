package non.era.aisdme.ui.components.over

import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.theme.aisDmeButtonBorder

@Composable
fun ButtonOk(
    enabled: Boolean = true,
    onClick: () -> Unit
) =
    Button(
        modifier = Modifier.width(90.dp),
        shape = AisDmeShapes.medium,
        border = aisDmeButtonBorder(),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = LocalAisDmeColors.current.primary,
            contentColor = LocalAisDmeColors.current.onPrimary
        ),
        enabled = enabled
    ) {
        Text("Ok")
    }