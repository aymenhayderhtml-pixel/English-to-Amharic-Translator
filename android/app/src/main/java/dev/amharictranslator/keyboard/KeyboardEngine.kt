package dev.amharictranslator.keyboard

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import dev.amharictranslator.data.AmharicTranslator
import dev.amharictranslator.data.DictionaryData
import dev.amharictranslator.data.LearningCache
import dev.amharictranslator.data.Suggestion

enum class CommitTrigger(val marker: Char) {
    Space(' '),
    Newline('\n'),
    Period('.'),
    Comma(','),
    Question('?'),
    Exclamation('!'),
    Semicolon(';'),
    Colon(':');

    companion object {
        fun fromChar(char: Char): CommitTrigger? {
            return entries.firstOrNull { it.marker == char }
        }
    }
}

data class KeyboardEditResult(
    val value: TextFieldValue,
    val currentToken: String,
    val committedLatinToken: String? = null,
    val committedAmharicText: String? = null
)

interface KeyboardSuggestionSource {
    fun prefixWordSuggestions(prefix: String, limit: Int): List<String>
    fun nextWordSuggestions(previousWord: String, prefix: String, limit: Int): List<String>
    fun wordFrequency(word: String): Int

    companion object {
        val Empty = object : KeyboardSuggestionSource {
            override fun prefixWordSuggestions(prefix: String, limit: Int): List<String> = emptyList()
            override fun nextWordSuggestions(previousWord: String, prefix: String, limit: Int): List<String> = emptyList()
            override fun wordFrequency(word: String): Int = 0
        }
    }
}

class LearningSuggestionSource(
    private val cache: LearningCache
) : KeyboardSuggestionSource {
    override fun prefixWordSuggestions(prefix: String, limit: Int): List<String> {
        return cache.wordCounts.entries
            .asSequence()
            .filter { prefix.isBlank() || it.key.startsWith(prefix) }
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key.length }
                    .thenBy { it.key }
            )
            .map { it.key }
            .take(limit)
            .toList()
    }

    override fun nextWordSuggestions(previousWord: String, prefix: String, limit: Int): List<String> {
        if (previousWord.isBlank()) return emptyList()

        return cache.bigramCounts.entries
            .asSequence()
            .mapNotNull { (key, count) ->
                val parts = key.split('|')
                if (parts.size != 2 || parts[0] != previousWord) {
                    return@mapNotNull null
                }

                val candidate = parts[1]
                if (prefix.isNotBlank() && !candidate.startsWith(prefix)) {
                    return@mapNotNull null
                }

                candidate to count
            }
            .sortedWith(
                compareByDescending<Pair<String, Int>> { it.second }
                    .thenBy { it.first.length }
                    .thenBy { it.first }
            )
            .map { it.first }
            .take(limit)
            .toList()
    }

    override fun wordFrequency(word: String): Int {
        return cache.wordCounts[word] ?: 0
    }
}

class KeyboardEngine(
    private val dictionary: DictionaryData,
    private val suggestionSource: KeyboardSuggestionSource = KeyboardSuggestionSource.Empty
) {
    fun processEdit(previous: TextFieldValue, next: TextFieldValue): KeyboardEditResult {
        if (!next.selection.collapsed) {
            return next.asResult()
        }

        val insertion = detectSimpleInsertion(previous, next)
            ?: return next.asResult()

        val trigger = CommitTrigger.fromChar(insertion.insertedChar)
            ?: return next.asResult()

        return commitLastToken(
            text = next.text,
            cursor = insertion.insertedAt + 1,
            trigger = trigger
        )
    }

    fun commitLastToken(text: String, cursor: Int, trigger: CommitTrigger): KeyboardEditResult {
        val safeCursor = cursor.coerceIn(0, text.length)
        val triggerIndex = safeCursor - 1

        if (triggerIndex !in text.indices || text[triggerIndex] != trigger.marker) {
            return TextFieldValue(text, TextRange(safeCursor)).asResult()
        }

        var tokenStart = triggerIndex
        while (tokenStart > 0 && isLatinTokenCharacter(text[tokenStart - 1])) {
            tokenStart -= 1
        }

        val tokenEnd = triggerIndex
        if (tokenStart == tokenEnd) {
            return TextFieldValue(text, TextRange(safeCursor)).asResult()
        }

        val rawToken = text.substring(tokenStart, tokenEnd)
        if (!AmharicTranslator.isAsciiLatinToken(rawToken)) {
            return TextFieldValue(text, TextRange(safeCursor)).asResult()
        }

        val normalizedToken = AmharicTranslator.normalizeWord(rawToken)
        val transliterated = AmharicTranslator.transliterateWord(normalizedToken, dictionary)
        if (!transliterated.changed) {
            return TextFieldValue(text, TextRange(safeCursor)).asResult()
        }

        val updatedText = buildString {
            append(text.substring(0, tokenStart))
            append(transliterated.text)
            append(text.substring(tokenEnd))
        }
        val updatedCursor = safeCursor + (transliterated.text.length - rawToken.length)
        val updatedValue = TextFieldValue(updatedText, TextRange(updatedCursor))

        return KeyboardEditResult(
            value = updatedValue,
            currentToken = currentTokenAtCursor(updatedValue),
            committedLatinToken = normalizedToken,
            committedAmharicText = transliterated.text
        )
    }

    fun suggest(currentToken: String, previousCommittedWord: String?): List<Suggestion> {
        val normalizedToken = AmharicTranslator.normalizeWord(currentToken)
        val normalizedPrevious = previousCommittedWord?.let(AmharicTranslator::normalizeWord).orEmpty()
        val suggestions = mutableListOf<Suggestion>()

        if (normalizedToken.isBlank()) {
            if (normalizedPrevious.isNotBlank()) {
                suggestionSource.nextWordSuggestions(normalizedPrevious, "", 4).forEach { latin ->
                    suggestions += Suggestion(
                        latin = latin,
                        amharic = AmharicTranslator.previewForSuggestion(latin, dictionary),
                        kind = "next-word"
                    )
                }
            }

            dictionary.syllableRules.take(6).forEach { rule ->
                suggestions += Suggestion(rule.latin, rule.amharic, "starter")
            }

            return suggestions.distinctBy { "${it.latin}:${it.amharic}" }
        }

        suggestionSource.nextWordSuggestions(normalizedPrevious, normalizedToken, 3).forEach { latin ->
            suggestions += Suggestion(
                latin = latin,
                amharic = AmharicTranslator.previewForSuggestion(latin, dictionary),
                kind = "next-word"
            )
        }

        suggestionSource.prefixWordSuggestions(normalizedToken, 4).forEach { latin ->
            suggestions += Suggestion(
                latin = latin,
                amharic = AmharicTranslator.previewForSuggestion(latin, dictionary),
                kind = "learned"
            )
        }

        dictionary.wordHints.keys
            .asSequence()
            .filter { it.startsWith(normalizedToken) }
            .take(4)
            .forEach { latin ->
                suggestions += Suggestion(latin, dictionary.wordHints.getValue(latin), "word")
            }

        dictionary.syllableRules
            .asSequence()
            .filter { it.latin.startsWith(normalizedToken) }
            .take(6)
            .forEach { rule ->
                suggestions += Suggestion(rule.latin, rule.amharic, "syllable")
            }

        return suggestions
            .distinctBy { "${it.latin}:${it.amharic}" }
            .sortedWith(
                compareByDescending<Suggestion> { suggestionSource.wordFrequency(it.latin) }
                    .thenBy { it.latin.length }
                    .thenBy { it.latin }
            )
            .take(8)
    }

    fun currentTokenAtCursor(value: TextFieldValue): String {
        return AmharicTranslator.currentLatinToken(value.text, value.selection.start)
    }

    private fun detectSimpleInsertion(previous: TextFieldValue, next: TextFieldValue): SimpleInsertion? {
        if (!previous.selection.collapsed || !next.selection.collapsed) return null
        if (next.text.length != previous.text.length + 1) return null

        var prefix = 0
        while (
            prefix < previous.text.length &&
            prefix < next.text.length &&
            previous.text[prefix] == next.text[prefix]
        ) {
            prefix += 1
        }

        var previousSuffix = previous.text.length - 1
        var nextSuffix = next.text.length - 1
        while (
            previousSuffix >= prefix &&
            nextSuffix > prefix &&
            previous.text[previousSuffix] == next.text[nextSuffix]
        ) {
            previousSuffix -= 1
            nextSuffix -= 1
        }

        val insertedText = next.text.substring(prefix, nextSuffix + 1)
        if (insertedText.length != 1) return null

        return SimpleInsertion(prefix, insertedText.first())
    }

    private fun TextFieldValue.asResult(): KeyboardEditResult {
        return KeyboardEditResult(
            value = this,
            currentToken = currentTokenAtCursor(this)
        )
    }

    private fun isLatinTokenCharacter(char: Char): Boolean {
        return char == '\'' || char in 'a'..'z' || char in 'A'..'Z'
    }

    private data class SimpleInsertion(
        val insertedAt: Int,
        val insertedChar: Char
    )
}
