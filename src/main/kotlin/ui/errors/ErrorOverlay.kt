package non.era.aisdme.ui.errors

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import non.era.aisdme.LocalAppController

//todo переделать под самоуплывающие
@Composable
fun ErrorOverlay() {
    val app = LocalAppController.current
    val errors = app.errors

    if (errors.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { app.clearErrors() },
            title = { Text("Error") },
            text = { Text(errors.last().text) },
            confirmButton = {
                Button(onClick = app::clearErrors) {
                    Text("OK")
                }
            }
        )
    }
}