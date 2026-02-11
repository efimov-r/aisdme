package non.era.aisdme.ui.errors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.LocalAisDmeColors
import non.era.aisdme.ui.components.over.SvgIcon

@Composable
fun GlobalMessageItem(
    modifier: Modifier = Modifier,
    mess: GlobalMessage,
    onClose: () -> Unit
) {
    Surface(
        elevation = 3.dp,
        shape = AisDmeShapes.medium,
        border = BorderStroke(1.dp, LocalAisDmeColors.current.surfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        val iconPadding = 12.dp
        val iconSize = 16.dp
        val endAndBottomPadding = 16.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            when (mess.type) {
                TypeGlobalMessage.INFO ->
                    SvgIcon("informationDialog", Modifier.padding(iconPadding).size(iconSize))
                TypeGlobalMessage.WARNING ->
                    SvgIcon("warningDialog", Modifier.padding(iconPadding).size(iconSize))
                TypeGlobalMessage.ERROR ->
                    SvgIcon("errorDialog", Modifier.padding(iconPadding).size(iconSize))
                TypeGlobalMessage.QUESTION ->
                    SvgIcon("questionDialog", Modifier.padding(iconPadding).size(iconSize))
                TypeGlobalMessage.SUCCESS ->
                    SvgIcon("successDialog", Modifier.padding(iconPadding).size(iconSize))
            }

//            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = mess.title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(top = 12.dp),
                        maxLines = 1,
                        style = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 16.sp),
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.padding(iconPadding).size(iconSize)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }


                Spacer(Modifier.height(4.dp))

                Text(
                    modifier = Modifier.padding(end = endAndBottomPadding, bottom = endAndBottomPadding),
                    text = mess.text,
                    style = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 16.sp),
//                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}