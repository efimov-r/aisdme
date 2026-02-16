package non.era.aisdme.ui.components.custom

import androidx.compose.runtime.*

@Composable
fun DialogableElement(
    dialog: @Composable (onDismis: () -> Unit) -> Unit,
    content: @Composable (showDialog: () -> Unit) -> Unit
) {
    var _showDialog by remember { mutableStateOf(false) }
    if (_showDialog) {
        dialog { _showDialog = false }
    }

    content{ _showDialog = true }
}