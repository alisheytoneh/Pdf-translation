package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ProcessingModeOption(
    val index: Int,
    val titleFa: String,
    val icon: ImageVector
)

val modeOptions = listOf(
    ProcessingModeOption(0, "ترجمه و خلاصه", Icons.Default.AutoAwesome),
    ProcessingModeOption(1, "ترجمه کامل", Icons.Default.Translate),
    ProcessingModeOption(2, "خلاصه سازی", Icons.Default.Summarize),
    ProcessingModeOption(3, "پرسش از سند", Icons.Default.HelpOutline)
)

@Composable
fun ModeSelector(
    selectedIndex: Int,
    onModeSelected: (Int) -> Unit,
    customQuestion: String,
    onQuestionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "نوع پردازش مورد نظر",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modeOptions.take(2).forEach { option ->
                val selected = option.index == selectedIndex
                FilterChip(
                    selected = selected,
                    onClick = { onModeSelected(option.index) },
                    label = { Text(text = option.titleFa, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_chip_${option.index}"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modeOptions.drop(2).forEach { option ->
                val selected = option.index == selectedIndex
                FilterChip(
                    selected = selected,
                    onClick = { onModeSelected(option.index) },
                    label = { Text(text = option.titleFa, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_chip_${option.index}"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        AnimatedVisibility(visible = selectedIndex == 3) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                OutlinedTextField(
                    value = customQuestion,
                    onValueChange = onQuestionChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_question_input"),
                    label = { Text("سوال شما درباره فایل PDF") },
                    placeholder = { Text("مثلاً: خلاصه بخش دوم یا تصمیمات اصلی فایل چیست؟") },
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3
                )
            }
        }
    }
}
