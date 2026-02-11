package non.era.aisdme.ui.errors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors

@Composable
fun ErrorItem(
    modifier: Modifier = Modifier,
    text: String,
    onClose: () -> Unit
) {
    Surface(
        elevation = 3.dp,
        shape = AisDmeShapes.medium,
        border = BorderStroke(1.dp, LocalAisDmeColors.current.surfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(12.dp)) {

            Text(
                text = text,
                modifier = Modifier.padding(end = 24.dp)
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}