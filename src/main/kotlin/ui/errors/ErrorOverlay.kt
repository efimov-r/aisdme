package non.era.aisdme.ui.errors

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import non.era.aisdme.LocalAppController
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.AisDmeSpacing
import non.era.aisdme.theme.LocalAisDmeColors

@Composable
fun ErrorOverlay() {
    val app = LocalAppController.current
    val errors = app.errors

    if (errors.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(999f) // поверх всего
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AisDmeSpacing.l)
                .width(320.dp)
                .heightIn(max = 600.dp)
        ) {

            LazyColumn(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
                ,reverseLayout = true // новые снизу
            ) {
                items(
                    items = errors,
                    key = { it.id }
                ) { error ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ErrorItem(
                            modifier = Modifier.padding(bottom = AisDmeSpacing.s),
                            text = error.text,
                            onClose = { app.removeError(error.id) }
                        )
                    }
                }
            }

            Button(
                onClick = app::clearErrors,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                border = BorderStroke(1.dp, LocalAisDmeColors.current.surfaceBorder),
                shape = AisDmeShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = LocalAisDmeColors.current.surface,
                    contentColor = LocalAisDmeColors.current.onSurface
                )
            ) {
                Text("Close all")
            }
        }
    }
}
