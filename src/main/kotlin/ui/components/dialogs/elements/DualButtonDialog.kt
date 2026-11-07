package non.era.aisdme.ui.components.dialogs.elements

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import non.era.aisdme.debug.debugTag
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.ui.components.over.ButtonCancel
import non.era.aisdme.ui.components.over.ButtonOk

@Composable
fun DualButtonDialog(
    modifier: Modifier = Modifier,
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    enterActive: () -> Boolean,
    content: @Composable (() -> Unit)
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = modifier
            .debugTag("dialog:$title")
            .onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when (it.key) {
                    Key.Enter, Key.NumPadEnter -> {
                        if(enterActive()) {
                            onConfirm()
                            onDismiss()
                        }
                        true
                    }
                    Key.Escape -> {
                        onDismiss()
                        true
                    }
                    else -> false
                }
            },
            shape = AisDmeShapes.medium,
            elevation = 8.dp
        ) {
            Column(Modifier.padding(vertical = 16.dp)) {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    Text(title)
                }

                Spacer(Modifier.height(12.dp))

                content()

                Spacer(Modifier.height(12.dp))

                Row(Modifier.align(Alignment.End)) {
                    ButtonCancel { onDismiss() }
                    Spacer(Modifier.width(8.dp))
                    ButtonOk {
                        if(enterActive()) {
                            onConfirm()
                            onDismiss()
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }
            }
        }
    }
}