package dev.amharictranslator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.amharictranslator.data.Suggestion
import dev.amharictranslator.ui.theme.AppColors

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    tag: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardDark),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary
                )
            }

            content()
        }
    }
}

@Composable
fun InfoLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = AppColors.TextSecondary
    )
}

@Composable
fun SuggestionRow(
    items: List<Suggestion>,
    accentColor: Color,
    onPick: (Suggestion) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            Surface(
                modifier = Modifier.clickable { onPick(item) },
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.latin,
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.amharic,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun MetricRow(
    learnedSessions: Int,
    uniqueWords: Int,
    uniquePhrases: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricChip("Sessions", learnedSessions.toString(), AppColors.Teal, Modifier.weight(1f))
        MetricChip("Words", uniqueWords.toString(), AppColors.Gold, Modifier.weight(1f))
        MetricChip("Phrases", uniquePhrases.toString(), AppColors.Terracotta, Modifier.weight(1f))
    }
}

@Composable
private fun MetricChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextMuted
            )
        }
    }
}

@Composable
fun OutputCard(
    label: String,
    accentColor: Color,
    output: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = accentColor.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = output,
                style = TextStyle(
                    color = AppColors.TextPrimary,
                    fontSize = 23.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
        }
    }
}

@Composable
fun KeyboardField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .testTag(testTag),
        value = value,
        onValueChange = onValueChange,
        minLines = 5,
        maxLines = 8,
        label = { Text("Type English letters here") },
        placeholder = { Text("Example: selam friend market", color = AppColors.TextMuted) },
        shape = RoundedCornerShape(18.dp),
        colors = textFieldColors(AppColors.Teal)
    )
}

@Composable
fun TranslatorField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        value = value,
        onValueChange = onValueChange,
        minLines = 4,
        maxLines = 6,
        label = { Text("English phrase or sentence") },
        placeholder = { Text("Example: good morning", color = AppColors.TextMuted) },
        shape = RoundedCornerShape(18.dp),
        colors = textFieldColors(AppColors.Gold)
    )
}

@Composable
private fun textFieldColors(accentColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accentColor,
    unfocusedBorderColor = AppColors.Border,
    focusedLabelColor = accentColor,
    unfocusedLabelColor = AppColors.TextMuted,
    cursorColor = accentColor,
    focusedContainerColor = AppColors.DeepNavy.copy(alpha = 0.45f),
    unfocusedContainerColor = AppColors.DeepNavy.copy(alpha = 0.25f),
    focusedTextColor = AppColors.TextPrimary,
    unfocusedTextColor = AppColors.TextPrimary
)

@Composable
fun ActionRow(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    tertiaryLabel: String,
    onTertiary: () -> Unit,
    accentColor: Color,
    primaryEnabled: Boolean = true,
    secondaryEnabled: Boolean = true,
    tertiaryEnabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor.copy(alpha = 0.18f),
                contentColor = accentColor
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(primaryLabel, fontWeight = FontWeight.SemiBold)
        }

        TextButton(
            onClick = onSecondary,
            enabled = secondaryEnabled,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(secondaryLabel, fontWeight = FontWeight.SemiBold)
        }

        TextButton(
            onClick = onTertiary,
            enabled = tertiaryEnabled,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(tertiaryLabel, fontWeight = FontWeight.SemiBold)
        }
    }
}
