package dev.amharictranslator.data

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

data class TranslationResult(
    val output: String,
    val mode: String,
    val confidence: String,
    val source: String
)

data class Suggestion(
    val latin: String,
    val amharic: String,
    val kind: String
)

data class TransliterationWord(
    val text: String,
    val changed: Boolean
)

data class TokenBounds(
    val start: Int,
    val endExclusive: Int
)

object AmharicTranslator {
    private val tokenRegex = Regex("[A-Za-z']+|[^A-Za-z']+")

    fun translate(input: String, dictionary: DictionaryData): TranslationResult {
        val normalized = normalizeEnglish(input)
        if (normalized.isBlank()) {
            return TranslationResult(
                output = "",
                mode = "Waiting for input",
                confidence = "Local-only preview",
                source = "Dictionary asset"
            )
        }

        dictionary.phrasebook[normalized]?.let { exact ->
            return TranslationResult(
                output = exact,
                mode = "Phrasebook match",
                confidence = "High confidence",
                source = "Dictionary asset"
            )
        }

        return TranslationResult(
            output = transliterate(input, dictionary),
            mode = "Transliteration fallback",
            confidence = "Local preview",
            source = "Dictionary asset"
        )
    }

    fun transliterate(input: String, dictionary: DictionaryData): String {
        if (input.isBlank()) return ""

        return tokenRegex.findAll(input)
            .joinToString(separator = "") { match ->
                val part = match.value
                if (isAsciiLatinToken(part)) {
                    val normalized = normalizeWord(part)
                    exactWordMatch(normalized, dictionary)
                        ?: transliterateWord(normalized, dictionary).text
                } else {
                    part
                }
            }
            .trim()
    }

    fun transliterateWord(word: String, dictionary: DictionaryData): TransliterationWord {
        val normalized = normalizeWord(word)
        if (normalized.isBlank()) {
            return TransliterationWord("", changed = false)
        }

        val exact = exactWordMatch(normalized, dictionary)
        if (exact != null) {
            return TransliterationWord(exact, changed = true)
        }

        val result = StringBuilder()
        var index = 0
        var changed = false

        while (index < normalized.length) {
            var matched = false

            for (rule in dictionary.syllableRules) {
                if (normalized.startsWith(rule.latin, index)) {
                    result.append(rule.amharic)
                    index += rule.latin.length
                    matched = true
                    changed = true
                    break
                }
            }

            if (!matched) {
                when (val current = normalized[index]) {
                    '\'' -> index += 1
                    '-' -> {
                        result.append(' ')
                        changed = true
                        index += 1
                    }
                    else -> {
                        result.append(current)
                        index += 1
                    }
                }
            }
        }

        return TransliterationWord(result.toString(), changed)
    }

    fun currentLatinToken(text: String, cursor: Int = text.length): String {
        val bounds = findCurrentLatinTokenBounds(text, cursor) ?: return ""
        return text.substring(bounds.start, bounds.endExclusive)
    }

    fun findCurrentLatinTokenBounds(text: String, cursor: Int = text.length): TokenBounds? {
        if (text.isEmpty()) return null
        val safeCursor = cursor.coerceIn(0, text.length)
        var start = safeCursor

        while (start > 0 && isLatinTokenCharacter(text[start - 1])) {
            start -= 1
        }

        var end = safeCursor
        while (end < text.length && isLatinTokenCharacter(text[end])) {
            end += 1
        }

        if (start == end) return null

        val token = text.substring(start, end)
        return if (isAsciiLatinToken(token)) TokenBounds(start, end) else null
    }

    fun replaceCurrentLatinToken(
        value: TextFieldValue,
        replacement: String
    ): TextFieldValue {
        val bounds = findCurrentLatinTokenBounds(value.text, value.selection.start)
            ?: return value

        val updatedText = buildString {
            append(value.text.substring(0, bounds.start))
            append(replacement)
            append(value.text.substring(bounds.endExclusive))
        }

        val newCursor = bounds.start + replacement.length
        return value.copy(
            text = updatedText,
            selection = TextRange(newCursor)
        )
    }

    fun previewForSuggestion(latin: String, dictionary: DictionaryData): String {
        val normalized = normalizeWord(latin)
        return exactWordMatch(normalized, dictionary)
            ?: transliterateWord(normalized, dictionary).text
    }

    fun normalizeEnglish(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s']"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun normalizeWord(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("[^a-z']"), "")
    }

    fun isAsciiLatinToken(value: String): Boolean {
        return value.isNotBlank() && value.all(::isLatinTokenCharacter)
    }

    private fun exactWordMatch(
        word: String,
        dictionary: DictionaryData
    ): String? {
        if (word.isBlank()) return null
        return dictionary.wordHints[word]
            ?: dictionary.phrasebook[word]?.takeIf { !it.contains(' ') || !word.contains(' ') }
    }

    private fun isLatinTokenCharacter(char: Char): Boolean {
        return char == '\'' || char in 'a'..'z' || char in 'A'..'Z'
    }
}
