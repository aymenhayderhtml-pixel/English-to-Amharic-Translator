package dev.amharictranslator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.amharictranslator.data.AmharicTranslator
import dev.amharictranslator.data.Suggestion
import dev.amharictranslator.data.TranslationResult
import dev.amharictranslator.ui.theme.AmharicTranslatorTheme
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmharicTranslatorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    TranslatorApp()
                }
            }
        }
    }
}

@Composable
fun TranslatorApp() {
    val scrollState = rememberScrollState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    var englishInput by rememberSaveable { mutableStateOf("good morning") }
    var typingInput by rememberSaveable { mutableStateOf("he hu hi ha hee h ho") }

    val translationResult = remember(englishInput) { AmharicTranslator.translate(englishInput) }
    val typingPreview = remember(typingInput) { AmharicTranslator.transliterate(typingInput) }
    val typingSuggestions = remember(typingInput) { AmharicTranslator.suggestions(typingInput) }

    val translatorExamples = remember {
        listOf("hello", "good morning", "thank you", "where is the market", "i am happy", "coffee", "please")
    }
    val keyboardExamples = remember {
        listOf("he", "hu", "hi", "ha", "hee", "h", "ho", "hua", "ae", "au", "ai", "aa")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1DF0B35D), Color.Transparent),
                    radius = 1400f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroCard()

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val wide = maxWidth >= 900.dp

                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TranslatorCard(
                            modifier = Modifier.weight(1f),
                            input = englishInput,
                            onInputChange = { englishInput = it },
                            result = translationResult,
                            examples = translatorExamples,
                            onExamplePick = { englishInput = it },
                            onCopy = {
                                if (translationResult.output.isNotBlank()) {
                                    clipboard.setText(AnnotatedString(translationResult.output))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        KeyboardLabCard(
                            modifier = Modifier.weight(1f),
                            input = typingInput,
                            onInputChange = { typingInput = it },
                            output = typingPreview,
                            token = AmharicTranslator.currentToken(typingInput),
                            suggestions = typingSuggestions,
                            examples = keyboardExamples,
                            onExamplePick = { typingInput = it },
                            onCopy = {
                                if (typingPreview.isNotBlank()) {
                                    clipboard.setText(AnnotatedString(typingPreview))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        TranslatorCard(
                            modifier = Modifier.fillMaxWidth(),
                            input = englishInput,
                            onInputChange = { englishInput = it },
                            result = translationResult,
                            examples = translatorExamples,
                            onExamplePick = { englishInput = it },
                            onCopy = {
                                if (translationResult.output.isNotBlank()) {
                                    clipboard.setText(AnnotatedString(translationResult.output))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        KeyboardLabCard(
                            modifier = Modifier.fillMaxWidth(),
                            input = typingInput,
                            onInputChange = { typingInput = it },
                            output = typingPreview,
                            token = AmharicTranslator.currentToken(typingInput),
                            suggestions = typingSuggestions,
                            examples = keyboardExamples,
                            onExamplePick = { typingInput = it },
                            onCopy = {
                                if (typingPreview.isNotBlank()) {
                                    clipboard.setText(AnnotatedString(typingPreview))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            InfoCard()
        }
    }
}

@Composable
private fun HeroCard() {
    AppCard(
        tag = "Offline-first prototype",
        title = "Amharic-English Offline Translator",
        description = "Translate common English phrases into Amharic and preview Latin-to-Amharic typing while staying fully local."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Offline-first prototype",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill("Offline-ready")
                StatusPill("Local data only")
                StatusPill("Kotlin + Compose")
            }
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TranslatorCard(
    modifier: Modifier,
    input: String,
    onInputChange: (String) -> Unit,
    result: TranslationResult,
    examples: List<String>,
    onExamplePick: (String) -> Unit,
    onCopy: () -> Unit
) {
    AppCard(modifier = modifier, tag = "Translator", title = "English to Amharic", description = "Phrasebook first, transliteration fallback second.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = input,
                onValueChange = onInputChange,
                label = { Text("English input") },
                placeholder = { Text("Type something like: good morning") },
                minLines = 4,
                maxLines = 6
            )

            SectionLabel("Try examples")
            ChipRow(
                items = examples.map { example ->
                    Suggestion(example, AmharicTranslator.translate(example).output, "example")
                },
                onPick = { onExamplePick(it.latin) }
            )

            ResultCard(
                title = "Amharic translation",
                subtitle = result.mode,
                confidence = result.confidence,
                output = result.output,
                onCopy = onCopy
            )
        }
    }
}

@Composable
private fun KeyboardLabCard(
    modifier: Modifier,
    input: String,
    onInputChange: (String) -> Unit,
    output: String,
    token: String,
    suggestions: List<Suggestion>,
    examples: List<String>,
    onExamplePick: (String) -> Unit,
    onCopy: () -> Unit
) {
    AppCard(modifier = modifier, tag = "Keyboard lab", title = "Live Amharic typing", description = "Preview how Latin typing maps to Amharic syllables.") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = input,
                onValueChange = onInputChange,
                label = { Text("Typing input") },
                placeholder = { Text("Try: he hu hi ha hee h ho") },
                minLines = 4,
                maxLines = 6
            )

            SectionLabel("Common syllables")
            ChipRow(
                items = examples.map { syllable ->
                    Suggestion(syllable, AmharicTranslator.transliterate(syllable), "syllable")
                },
                onPick = { onExamplePick(it.latin) }
            )

            ResultCard(
                title = "Current Amharic output",
                subtitle = if (token.isBlank()) "No token yet" else "Token: $token",
                confidence = "Prototype preview",
                output = output,
                onCopy = onCopy
            )

            SectionLabel("Suggestions")
            if (suggestions.isEmpty()) {
                Text(
                    text = "No suggestions yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ChipRow(
                    items = suggestions,
                    onPick = { onExamplePick(it.latin) }
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    AppCard(tag = "How it works", title = "Local-only MVP logic", description = "The Android app keeps the first build small and offline.") {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            InfoBlock("Phrasebook", "Common phrases are translated from a local mapping first.")
            InfoBlock("Fallback", "Unknown English text is transliterated with an offline rule set.")
            InfoBlock("Next step", "Later we can swap the input layer for Keyman-powered keyboards and richer prediction.")
        }
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AppCard(
    modifier: Modifier = Modifier,
    tag: String,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = tag,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ChipRow(items: List<Suggestion>, onPick: (Suggestion) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            AssistChip(
                onClick = { onPick(item) },
                label = {
                    Text(
                        text = "${item.latin} -> ${item.amharic}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun ResultCard(
    title: String,
    subtitle: String,
    confidence: String,
    output: String,
    onCopy: () -> Unit
) {
    val isEmpty = output.isBlank()

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = title, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                TextButton(
                    onClick = onCopy,
                    enabled = !isEmpty
                ) {
                    Text("Copy")
                }
            }

            SelectionContainer {
                Text(
                    text = if (isEmpty) "Your Amharic output will appear here." else output,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = confidence,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
