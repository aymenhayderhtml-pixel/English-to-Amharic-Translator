package dev.amharictranslator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.amharictranslator.data.LearningCache
import dev.amharictranslator.data.Suggestion
import dev.amharictranslator.ui.theme.AppColors

@Composable
fun KeyboardScreen(
    keyboardValue: TextFieldValue,
    onKeyboardValueChange: (TextFieldValue) -> Unit,
    currentToken: String,
    suggestions: List<Suggestion>,
    recentWordSuggestions: List<Suggestion>,
    learningCache: LearningCache,
    onSuggestionPick: (Suggestion) -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit
) {
    SectionCard(
        accentColor = AppColors.Teal,
        tag = "KEYBOARD",
        title = "Live Amharic keyboard",
        subtitle = "Press space, enter, or punctuation to convert the last word."
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoLine("Only the token before the cursor is converted. Mixed text and backspace stay safe.")

            KeyboardField(
                value = keyboardValue,
                onValueChange = onKeyboardValueChange,
                testTag = "keyboard_input"
            )

            ActionRow(
                primaryLabel = "Copy",
                onPrimary = onCopy,
                secondaryLabel = "Share",
                onSecondary = onShare,
                tertiaryLabel = "Clear",
                onTertiary = onClear,
                accentColor = AppColors.Teal,
                primaryEnabled = keyboardValue.text.isNotBlank(),
                secondaryEnabled = keyboardValue.text.isNotBlank(),
                tertiaryEnabled = keyboardValue.text.isNotBlank()
            )

            OutputCard(
                label = "Current text",
                accentColor = AppColors.Teal,
                output = keyboardValue.text.ifBlank { "Committed text appears here." },
                supporting = if (currentToken.isBlank()) {
                    "No active token."
                } else {
                    "Active token: $currentToken"
                }
            )

            InfoLine("Suggestions follow the current token, not the whole field.")
            if (suggestions.isEmpty()) {
                InfoLine("No suggestions yet.")
            } else {
                SuggestionRow(
                    items = suggestions,
                    accentColor = AppColors.Teal,
                    onPick = onSuggestionPick
                )
            }

            MetricRow(
                learnedSessions = learningCache.learnedSessions,
                uniqueWords = learningCache.uniqueWords,
                uniquePhrases = learningCache.uniquePhrases
            )

            if (recentWordSuggestions.isNotEmpty()) {
                InfoLine("Recent words stored locally on this device")
                SuggestionRow(
                    items = recentWordSuggestions,
                    accentColor = AppColors.Terracotta,
                    onPick = onSuggestionPick
                )
            }
        }
    }
}
