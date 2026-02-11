package non.era.aisdme.ui.errors

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import non.era.aisdme.LocalAppController
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
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(320.dp)
                .heightIn(max = 400.dp, min = 10.dp) // ограничение высоты
                .background(
                    LocalAisDmeColors.current.custom,
//                    colors.surface.copy(alpha = 0.95f),
                    RoundedCornerShape(12.dp)
                )
        ) {

            // === список ошибок ===
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .background(LocalAisDmeColors.current.secondary)
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
                            text = error.text,
                            onClose = { app.removeError(error.id) }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            Button(
                onClick = app::clearErrors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Close all")
            }
        }
    }
}
