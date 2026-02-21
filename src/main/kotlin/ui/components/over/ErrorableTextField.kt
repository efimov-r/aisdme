package non.era.aisdme.ui.components.over

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import non.era.aisdme.theme.LocalAisDmeColors

@Composable
fun ErrorableTextField(
    initial: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusRequester: FocusRequester,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(90.dp)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            isError = error != null,
            singleLine = true,
            placeholder = { Text(initial) }, // Вместо label используем placeholder
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = LocalAisDmeColors.current.primary, // Цвет черты при фокусе
                unfocusedIndicatorColor = LocalAisDmeColors.current.surfaceBorder, // Цвет черты без фокуса
                disabledIndicatorColor = LocalAisDmeColors.current.surfaceBorder, // Цвет черты для disabled
                errorIndicatorColor = LocalAisDmeColors.current.error, // Цвет черты при ошибке
                cursorColor = LocalAisDmeColors.current.primary, // Цвет курсора
                backgroundColor = LocalAisDmeColors.current.transparent // Прозрачный фон
            )
        )

        if (error != null) {
            Text(
                error,
                color = LocalAisDmeColors.current.error,
                style = LocalTextStyle.current.copy(fontSize = 12.sp, lineHeight = 20.sp)
            )
        }
    }
}