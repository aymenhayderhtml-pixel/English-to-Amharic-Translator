package dev.amharictranslator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.amharictranslator.data.LearningCache
import dev.amharictranslator.data.Suggestion
import dev.amharictranslator.data.TranslationResult
import dev.amharictranslator.ui.theme.AppColors

@Composable
fun TranslatorScreen(
    translatorInput: String,
    onTranslatorInputChange: (String) -> Unit,
    translationResult: TranslationResult,
    exampleSuggestions: List<Suggestion>,
    recentPhraseSuggestions: List<Suggestion>,
    learningCache: LearningCache,
    onExamplePick: (Suggestion) -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onTeach: () -> Unit
) {
    SectionCard(
        accentColor = AppColors.Gold,
        tag = "TRANSLATOR",
        title = "Translate full phrases separately",
        subtitle = "Use this when you want a phrasebook result or a full-sentence preview."
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoLine("Translation stays separate from typing so the keyboard can stay fast and predictable.")

            TranslatorField(
                value = translatorInput,
                onValueChange = onTranslatorInputChange,
                testTag = "translator_input"
            )

            SuggestionRow(
                items = exampleSuggestions,
                accentColor = AppColors.Gold,
                onPick = onExamplePick
            )

            OutputCard(
                label = "Amharic translation",
                accentColor = AppColors.Gold,
                output = translationResult.output.ifBlank { "Your Amharic translation will appear here." },
                supporting = "${translationResult.mode} | ${translationResult.confidence}"
            )

            ActionRow(
                primaryLabel = "Copy",
                onPrimary = onCopy,
                secondaryLabel = "Share",
                onSecondary = onShare,
                tertiaryLabel = "Teach",
                onTertiary = onTeach,
                accentColor = AppColors.Gold,
                primaryEnabled = translationResult.output.isNotBlank(),
                secondaryEnabled = translationResult.output.isNotBlank(),
                tertiaryEnabled = translatorInput.isNotBlank()
            )

            MetricRow(
                learnedSessions = learningCache.learnedSessions,
                uniqueWords = learningCache.uniqueWords,
                uniquePhrases = learningCache.uniquePhrases
            )

            if (recentPhraseSuggestions.isNotEmpty()) {
                InfoLine("Recent phrases stored locally on this device")
                SuggestionRow(
                    items = recentPhraseSuggestions,
                    accentColor = AppColors.Terracotta,
                    onPick = onExamplePick
                )
            }
        }
    }
}
