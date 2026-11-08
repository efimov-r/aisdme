package non.era.aisdme.ui.errors.view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.i18n.I18n
import non.era.aisdme.theme.AisDmeShapes
import non.era.aisdme.theme.aisDmeButtonBorder
import non.era.aisdme.ui.components.over.SvgIcon
import non.era.aisdme.ui.errors.GlobalMessage
import non.era.aisdme.ui.errors.TypeGlobalMessage

private val MAX_TEXT_HEIGHT = 320.dp

@Composable
fun GlobalMessageItem(
    modifier: Modifier = Modifier,
    mess: GlobalMessage,
    onClose: () -> Unit
) {
    Surface(
        elevation = 3.dp,
        shape = AisDmeShapes.medium,
        border = aisDmeButtonBorder(),
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
                        Icon(Icons.Default.Close, contentDescription = I18n.s.close)
                    }
                }


                Spacer(Modifier.height(4.dp))

                MessageText(mess.text, Modifier.padding(end = endAndBottomPadding, bottom = endAndBottomPadding))
            }
        }
    }
}

@Composable
private fun MessageText(text: String, modifier: Modifier = Modifier) {
    val scroll = rememberScrollState()

    Box(modifier.heightIn(max = MAX_TEXT_HEIGHT)) {
        Text( //длинный текст (stack trace) прокручивается внутри карточки, а не вылезает за окно
            modifier = Modifier.verticalScroll(scroll).padding(end = 10.dp),
            text = text.replace("\t", "    "), //таб шрифт не рисует — квадратик
            style = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 16.sp),
        )
        if (scroll.maxValue > 0) {
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(8.dp),
                adapter = rememberScrollbarAdapter(scroll),
            )
        }
    }
}