package dev.amharictranslator

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.amharictranslator.data.AmharicTranslator
import dev.amharictranslator.data.SmartLearningEngine
import dev.amharictranslator.data.SmartLearningPreview
import dev.amharictranslator.data.Suggestion
import dev.amharictranslator.data.TranslationResult
import dev.amharictranslator.ui.theme.AmharicTranslatorTheme
import dev.amharictranslator.ui.theme.AppColors

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

// ═══════════════════════════════════════════════════════════════════
//  Root screen
// ═══════════════════════════════════════════════════════════════════

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TranslatorApp() {
    val scrollState = rememberScrollState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val learningEngine = remember(context) { SmartLearningEngine(context.applicationContext) }

    var englishInput by rememberSaveable { mutableStateOf("good morning") }
    var typingInput by rememberSaveable { mutableStateOf("he hu hi ha hee h ho") }
    var learningVersion by remember { mutableStateOf(0) }

    val translationResult = remember(englishInput) { AmharicTranslator.translate(englishInput) }
    val typingPreview = remember(typingInput) { AmharicTranslator.transliterate(typingInput) }
    val typingSuggestions = remember(typingInput) { AmharicTranslator.suggestions(typingInput) }
    val smartLearningPreview = remember(englishInput, learningVersion) {
        learningEngine.preview(englishInput)
    }

    val translatorExamples = remember {
        listOf("hello", "good morning", "thank you", "where is the market", "i am happy", "coffee", "please")
    }
    val keyboardExamples = remember {
        listOf("he", "hu", "hi", "ha", "hee", "h", "ho", "hua", "ae", "au", "ai", "aa")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle warm glow at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.Gold.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HeroSection()

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val wide = maxWidth >= 900.dp

                if (wide) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    TranslationSection(
                            modifier = Modifier.fillMaxWidth(0.49f),
                            input = englishInput,
                            onInputChange = { englishInput = it },
                            result = translationResult,
                            examples = translatorExamples,
                            onExamplePick = {
                                englishInput = it
                                learningEngine.learnPhrase(it)
                                learningVersion += 1
                            },
                            onCopy = {
                                if (translationResult.output.isNotBlank()) {
                                    clipboard.setText(AnnotatedString(translationResult.output))
                                    learningEngine.learnPhrase(englishInput)
                                    learningVersion += 1
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        KeyboardSection(
                            modifier = Modifier.fillMaxWidth(0.49f),
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
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        TranslationSection(
                            modifier = Modifier.fillMaxWidth(),
                            input = englishInput,
                            onInputChange = { englishInput = it },
                            result = translationResult,
                            examples = translatorExamples,
                            onExamplePick = {
                                englishInput = it
                                learningEngine.learnPhrase(it)
                                learningVersion += 1
                            },
                            onCopy = {
                                if (translationResult.output.isNotBlank()) {
                                    clipboard.setText(AnnotatedString(translationResult.output))
                                    learningEngine.learnPhrase(englishInput)
                                    learningVersion += 1
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        KeyboardSection(
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
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            SmartLearningSection(
                modifier = Modifier.fillMaxWidth(),
                input = englishInput,
                preview = smartLearningPreview,
                onTrain = {
                    if (englishInput.isNotBlank()) {
                        learningEngine.learnPhrase(englishInput)
                        learningVersion += 1
                        Toast.makeText(context, "Learning updated", Toast.LENGTH_SHORT).show()
                    }
                },
                onReset = {
                    learningEngine.reset()
                    learningVersion += 1
                    Toast.makeText(context, "Learning reset", Toast.LENGTH_SHORT).show()
                }
            )

            FeaturesSection()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Hero header
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AppColors.CardDark, AppColors.CardMedium.copy(alpha = 0.6f))
                )
            )
    ) {
        // Decorative Amharic watermark
        Text(
            text = "አማ",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 16.dp),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
            color = AppColors.Gold.copy(alpha = 0.05f)
        )

        Column {
            // Top accent gradient bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                AppColors.Gold,
                                AppColors.Teal.copy(alpha = 0.6f),
                                AppColors.Terracotta.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tag badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Gold.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "OFFLINE TRANSLATOR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Gold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Text(
                    text = "Amharic\nOffline Translator",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppColors.TextPrimary,
                    lineHeight = 36.sp
                )

                Text(
                    text = "Translate English phrases to Amharic and preview live Latin-to-Ge\u2019ez typing \u2014 fully offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    StatusPill("Offline-ready", AppColors.Gold)
                    StatusPill("Local data", AppColors.Teal)
                    StatusPill("Compose", AppColors.Terracotta)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Reusable section card wrapper
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    tag: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardDark),
        border = BorderStroke(1.dp, AppColors.Border.copy(alpha = 0.7f))
    ) {
        Column {
            // Accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.2f))
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section header
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }

                content()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Translation section
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun TranslationSection(
    modifier: Modifier,
    input: String,
    onInputChange: (String) -> Unit,
    result: TranslationResult,
    examples: List<String>,
    onExamplePick: (String) -> Unit,
    onCopy: () -> Unit
) {
    SectionCard(
        modifier = modifier,
        accentColor = AppColors.Gold,
        tag = "TRANSLATE",
        title = "English \u2192 Amharic",
        subtitle = "Phrasebook first, transliteration fallback"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.animateContentSize(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        ) {
            StyledTextField(
                value = input,
                onValueChange = onInputChange,
                label = "Type in English",
                placeholder = "e.g. good morning",
                accentColor = AppColors.Gold
            )

            SectionLabel("Try an example")
            ChipRow(
                items = examples.map { ex ->
                    Suggestion(ex, AmharicTranslator.translate(ex).output, "example")
                },
                accentColor = AppColors.Gold,
                onPick = { onExamplePick(it.latin) }
            )

            ResultCard(
                accentColor = AppColors.Gold,
                label = "Amharic Translation",
                modeTag = result.mode,
                confidence = result.confidence,
                output = result.output,
                onCopy = onCopy
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Keyboard lab section
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun KeyboardSection(
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
    SectionCard(
        modifier = modifier,
        accentColor = AppColors.Teal,
        tag = "KEYBOARD LAB",
        title = "Live Amharic Typing",
        subtitle = "Preview how Latin typing maps to Amharic syllables"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.animateContentSize(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        ) {
            StyledTextField(
                value = input,
                onValueChange = onInputChange,
                label = "Typing input",
                placeholder = "Try: he hu hi ha hee h ho",
                accentColor = AppColors.Teal
            )

            SectionLabel("Common syllables")
            ChipRow(
                items = examples.map { syl ->
                    Suggestion(syl, AmharicTranslator.transliterate(syl), "syllable")
                },
                accentColor = AppColors.Teal,
                onPick = { onExamplePick(it.latin) }
            )

            ResultCard(
                accentColor = AppColors.Teal,
                label = "Amharic Output",
                modeTag = if (token.isBlank()) "No token yet" else "Token: $token",
                confidence = "Prototype preview",
                output = output,
                onCopy = onCopy
            )

            SectionLabel("Suggestions")
            if (suggestions.isEmpty()) {
                Text(
                    text = "No suggestions yet.",
                    color = AppColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                ChipRow(
                    items = suggestions,
                    accentColor = AppColors.Terracotta,
                    onPick = { onExamplePick(it.latin) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Features / info section
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FeaturesSection() {
    SectionCard(
        accentColor = AppColors.Terracotta,
        tag = "HOW IT WORKS",
        title = "Local-Only MVP",
        subtitle = "Everything runs on your device"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureItem("01", "Phrasebook Lookup", "Common phrases matched from a curated local dictionary.", AppColors.Gold)
            FeatureItem("02", "Transliteration Fallback", "Unknown text is mapped through offline syllable rules.", AppColors.Teal)
            FeatureItem("03", "Smart Autocorrect", "The app learns approved phrases locally and suggests better next words.", AppColors.Terracotta)
            FeatureItem("04", "Future: Keyman Integration", "Full keyboard support with richer prediction coming soon.", AppColors.Gold)
        }
    }
}

@Composable
private fun SmartLearningSection(
    modifier: Modifier,
    input: String,
    preview: SmartLearningPreview,
    onTrain: () -> Unit,
    onReset: () -> Unit
) {
    SectionCard(
        modifier = modifier,
        accentColor = AppColors.Teal,
        tag = "SMART AUTOCORRECT",
        title = "Local learning assistant",
        subtitle = "Learns from phrases you accept on-device"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.animateContentSize(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        ) {
            Text(
                text = "This mini AI keeps a private memory of the words and phrases you teach it. It never sends your text online.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )

            ResultCard(
                accentColor = AppColors.Teal,
                label = "Autocorrect Preview",
                modeTag = preview.correctionLabel,
                confidence = preview.confidence,
                output = preview.correctedText.ifBlank { input.trim() },
                onCopy = onTrain,
                actionLabel = "Teach"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    label = "Sessions",
                    value = preview.learnedSessions.toString(),
                    accentColor = AppColors.Teal,
                    modifier = Modifier.fillMaxWidth(0.31f)
                )
                StatChip(
                    label = "Words",
                    value = preview.uniqueWords.toString(),
                    accentColor = AppColors.Gold,
                    modifier = Modifier.fillMaxWidth(0.31f)
                )
                StatChip(
                    label = "Phrases",
                    value = preview.uniquePhrases.toString(),
                    accentColor = AppColors.Terracotta,
                    modifier = Modifier.fillMaxWidth(0.31f)
                )
            }

            SectionLabel("Next predictions")
            if (preview.predictions.isEmpty()) {
                Text(
                    text = "Type a few phrases and tap Teach this phrase to build local predictions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextMuted
                )
            } else {
                ChipRow(
                    items = preview.predictions.map { word ->
                        Suggestion(
                            latin = word,
                            amharic = AmharicTranslator.transliterate(word),
                            kind = "prediction"
                        )
                    },
                    accentColor = AppColors.Teal,
                    onPick = { }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onTrain,
                    modifier = Modifier.fillMaxWidth(0.46f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Teal.copy(alpha = 0.20f),
                        contentColor = AppColors.Teal
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Teach this phrase",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth(0.46f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Reset memory",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
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
private fun FeatureItem(number: String, title: String, description: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.DeepNavy.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Shared components
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    accentColor: Color
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = AppColors.TextMuted) },
        minLines = 3,
        maxLines = 5,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = AppColors.Border,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = AppColors.TextMuted,
            cursorColor = accentColor,
            focusedContainerColor = AppColors.DeepNavy.copy(alpha = 0.5f),
            unfocusedContainerColor = AppColors.DeepNavy.copy(alpha = 0.3f),
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary
        )
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = AppColors.TextMuted,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun ChipRow(items: List<Suggestion>, accentColor: Color, onPick: (Suggestion) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Surface(
                onClick = { onPick(item) },
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.latin,
                        style = MaterialTheme.typography.labelMedium,
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
private fun ResultCard(
    accentColor: Color,
    label: String,
    modeTag: String,
    confidence: String,
    output: String,
    onCopy: () -> Unit,
    actionLabel: String = "Copy"
) {
    val isEmpty = output.isBlank()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = modeTag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor
                        )
                    }
                }

                Button(
                    onClick = onCopy,
                    enabled = !isEmpty,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor = accentColor,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = AppColors.TextMuted
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Amharic output
            SelectionContainer {
                Text(
                    text = if (isEmpty) "Your translation will appear here\u2026" else output,
                    style = if (isEmpty) {
                        MaterialTheme.typography.bodyLarge
                    } else {
                        MaterialTheme.typography.headlineMedium.copy(lineHeight = 38.sp)
                    },
                    color = if (isEmpty) AppColors.TextMuted else AppColors.TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                )
            }

            // Confidence
            Text(
                text = confidence,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextMuted
            )
        }
    }
}
