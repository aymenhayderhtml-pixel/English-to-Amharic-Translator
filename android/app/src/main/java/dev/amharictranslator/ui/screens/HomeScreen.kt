package dev.amharictranslator.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.amharictranslator.data.AmharicTranslator
import dev.amharictranslator.data.DictionaryData
import dev.amharictranslator.data.LearningCache
import dev.amharictranslator.data.LearningRepository
import dev.amharictranslator.data.LearningSettings
import dev.amharictranslator.data.Suggestion
import dev.amharictranslator.keyboard.CommitTrigger
import dev.amharictranslator.keyboard.KeyboardEditResult
import dev.amharictranslator.keyboard.KeyboardEngine
import dev.amharictranslator.keyboard.LearningSuggestionSource
import dev.amharictranslator.ui.theme.AppColors
import kotlinx.coroutines.launch

private enum class AppTab(val title: String) {
    Keyboard("Keyboard"),
    Translator("Translator")
}

@Composable
fun HomeScreen(
    dictionary: DictionaryData,
    learningRepository: LearningRepository
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Keyboard) }
    var keyboardValue by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var translatorInput by rememberSaveable { mutableStateOf("") }
    var learningCache by remember { mutableStateOf(LearningCache()) }
    var lastCommittedWord by rememberSaveable { mutableStateOf<String?>(null) }
    var restoredState by remember { mutableStateOf(false) }

    suspend fun refreshLearningCache() {
        learningCache = learningRepository.loadCache()
    }

    LaunchedEffect(Unit) {
        val savedKeyboardText = learningRepository.getSetting(LearningSettings.KEYBOARD_DRAFT)
        val savedKeyboardCursor = learningRepository.getSetting(LearningSettings.KEYBOARD_CURSOR, "0")
            .toIntOrNull()
            ?: savedKeyboardText.length
        keyboardValue = TextFieldValue(
            text = savedKeyboardText,
            selection = TextRange(savedKeyboardCursor.coerceIn(0, savedKeyboardText.length))
        )
        translatorInput = learningRepository.getSetting(LearningSettings.TRANSLATOR_DRAFT)
        lastCommittedWord = learningRepository.getSetting(LearningSettings.LAST_COMMITTED_WORD).ifBlank { null }
        refreshLearningCache()
        restoredState = true
    }

    val keyboardEngine = remember(dictionary, learningCache) {
        KeyboardEngine(dictionary, LearningSuggestionSource(learningCache))
    }
    val currentToken = remember(keyboardValue, keyboardEngine) {
        keyboardEngine.currentTokenAtCursor(keyboardValue)
    }
    val keyboardSuggestions = remember(currentToken, lastCommittedWord, keyboardEngine) {
        keyboardEngine.suggest(currentToken, lastCommittedWord)
    }
    val translatorResult = remember(translatorInput, dictionary) {
        AmharicTranslator.translate(translatorInput, dictionary)
    }
    val translatorExamples = remember(dictionary) {
        listOf(
            "hello",
            "good morning",
            "thank you",
            "where is the market",
            "i am happy",
            "coffee"
        ).map { example ->
            Suggestion(
                latin = example,
                amharic = AmharicTranslator.translate(example, dictionary).output,
                kind = "example"
            )
        }
    }
    val recentWordSuggestions = remember(learningCache, dictionary) {
        learningCache.recentWords.map { word ->
            Suggestion(
                latin = word,
                amharic = AmharicTranslator.previewForSuggestion(word, dictionary),
                kind = "recent-word"
            )
        }
    }
    val recentPhraseSuggestions = remember(learningCache, dictionary) {
        learningCache.recentPhrases.map { phrase ->
            Suggestion(
                latin = phrase,
                amharic = AmharicTranslator.translate(phrase, dictionary).output,
                kind = "recent-phrase"
            )
        }
    }

    fun persistKeyboardState(value: TextFieldValue) {
        if (!restoredState) return

        coroutineScope.launch {
            learningRepository.setSetting(LearningSettings.KEYBOARD_DRAFT, value.text)
            learningRepository.setSetting(LearningSettings.KEYBOARD_CURSOR, value.selection.start.toString())
        }
    }

    fun persistTranslatorState(value: String) {
        if (!restoredState) return

        coroutineScope.launch {
            learningRepository.setSetting(LearningSettings.TRANSLATOR_DRAFT, value)
        }
    }

    fun applyKeyboardResult(result: KeyboardEditResult) {
        val previousCommittedWord = lastCommittedWord
        keyboardValue = result.value
        persistKeyboardState(result.value)

        result.committedLatinToken?.let { committedWord ->
            lastCommittedWord = committedWord
            coroutineScope.launch {
                learningRepository.recordCommittedWord(committedWord, previousCommittedWord)
                learningRepository.setSetting(LearningSettings.LAST_COMMITTED_WORD, committedWord)
                refreshLearningCache()
            }
        }
    }

    fun copyText(text: String, message: String) {
        if (text.isBlank()) return
        clipboard.setText(AnnotatedString(text))
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun shareText(text: String, chooserTitle: String) {
        if (text.isBlank()) return

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    fun pickSuggestion(suggestion: Suggestion) {
        val hasCurrentToken = currentToken.isNotBlank()
        val preparedValue = if (hasCurrentToken) {
            AmharicTranslator.replaceCurrentLatinToken(keyboardValue, suggestion.latin)
        } else {
            val insertionPoint = keyboardValue.selection.start
            val updatedText = buildString {
                append(keyboardValue.text.substring(0, insertionPoint))
                append(suggestion.latin)
                append(keyboardValue.text.substring(insertionPoint))
            }
            TextFieldValue(updatedText, TextRange(insertionPoint + suggestion.latin.length))
        }

        val textWithSpace = buildString {
            append(preparedValue.text.substring(0, preparedValue.selection.start))
            append(' ')
            append(preparedValue.text.substring(preparedValue.selection.start))
        }

        applyKeyboardResult(
            keyboardEngine.commitLastToken(
                text = textWithSpace,
                cursor = preparedValue.selection.start + 1,
                trigger = CommitTrigger.Space
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.Gold.copy(alpha = 0.08f),
                            AppColors.Teal.copy(alpha = 0.03f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppColors.CardDark,
                    border = BorderStroke(1.dp, AppColors.Border)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Amharic Offline Keyboard",
                            style = MaterialTheme.typography.headlineLarge.copy(lineHeight = 34.sp),
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = "Free, offline, and built for v1 shipping: the keyboard commits the last English word into Amharic when you press space, enter, or punctuation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = "All learning is stored locally on this device.",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppColors.Gold,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    learningRepository.resetLearning()
                                    lastCommittedWord = null
                                    refreshLearningCache()
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Reset local memory")
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppColors.CardMedium.copy(alpha = 0.9f)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = AppColors.TextPrimary
                    ) {
                        AppTab.entries.forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }
            }

            item {
                when (selectedTab) {
                    AppTab.Keyboard -> KeyboardScreen(
                        keyboardValue = keyboardValue,
                        onKeyboardValueChange = { newValue ->
                            applyKeyboardResult(
                                keyboardEngine.processEdit(
                                    previous = keyboardValue,
                                    next = newValue
                                )
                            )
                        },
                        currentToken = currentToken,
                        suggestions = keyboardSuggestions,
                        recentWordSuggestions = recentWordSuggestions,
                        learningCache = learningCache,
                        onSuggestionPick = ::pickSuggestion,
                        onCopy = { copyText(keyboardValue.text, "Keyboard text copied") },
                        onShare = { shareText(keyboardValue.text, "Share Amharic text") },
                        onClear = {
                            keyboardValue = TextFieldValue()
                            persistKeyboardState(keyboardValue)
                        }
                    )

                    AppTab.Translator -> TranslatorScreen(
                        translatorInput = translatorInput,
                        onTranslatorInputChange = {
                            translatorInput = it
                            persistTranslatorState(it)
                        },
                        translationResult = translatorResult,
                        exampleSuggestions = translatorExamples,
                        recentPhraseSuggestions = recentPhraseSuggestions,
                        learningCache = learningCache,
                        onExamplePick = { suggestion ->
                            translatorInput = suggestion.latin
                            persistTranslatorState(suggestion.latin)
                        },
                        onCopy = {
                            copyText(translatorResult.output, "Translation copied")
                            coroutineScope.launch {
                                learningRepository.recordAcceptedPhrase(translatorInput)
                                refreshLearningCache()
                            }
                        },
                        onShare = {
                            shareText(translatorResult.output, "Share translated text")
                            coroutineScope.launch {
                                learningRepository.recordAcceptedPhrase(translatorInput)
                                refreshLearningCache()
                            }
                        },
                        onTeach = {
                            coroutineScope.launch {
                                learningRepository.recordAcceptedPhrase(translatorInput)
                                refreshLearningCache()
                            }
                        }
                    )
                }
            }
        }
    }
}
