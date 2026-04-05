package dev.amharictranslator.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

data class SmartLearningPreview(
    val correctedText: String,
    val correctionLabel: String,
    val confidence: String,
    val predictions: List<String>,
    val nextWordSuggestions: List<String>,
    val nextPhraseSuggestions: List<String>,
    val recentPhrases: List<String>,
    val recentWords: List<String>,
    val learnedSessions: Int,
    val uniqueWords: Int,
    val uniquePhrases: Int
)

data class SmartLearningStats(
    val learnedSessions: Int,
    val uniqueWords: Int,
    val uniquePhrases: Int,
    val uniqueBigrams: Int
)

data class SmartLearningHistory(
    val recentPhrases: List<String>,
    val recentWords: List<String>
)

class SmartLearningEngine(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val wordCounts = mutableMapOf<String, Int>()
    private val phraseCounts = mutableMapOf<String, Int>()
    private val bigramCounts = mutableMapOf<String, Int>()
    private val recentPhrases = mutableListOf<String>()
    private val recentWords = mutableListOf<String>()
    private val lexicalEntries = LocalLexicalModel.loadEntries(context)

    private var learnedSessions: Int = 0

    init {
        loadState()
    }

    fun preview(input: String): SmartLearningPreview {
        val normalized = normalize(input)
        val stats = stats()

        if (normalized.isBlank()) {
            return SmartLearningPreview(
                correctedText = "",
                correctionLabel = "Waiting for input",
                confidence = "Local learning only",
                predictions = topPhrases(4),
                nextWordSuggestions = emptyList(),
                nextPhraseSuggestions = topPhrases(4),
                recentPhrases = recentPhrases.take(MAX_RECENT),
                recentWords = recentWords.take(MAX_RECENT),
                learnedSessions = stats.learnedSessions,
                uniqueWords = stats.uniqueWords,
                uniquePhrases = stats.uniquePhrases
            )
        }

        val phraseCorrection = bestPhraseMatch(normalized)
        val correctedText = when {
            phraseCorrection != null -> phraseCorrection
            else -> autocorrectByToken(input)
        }

        val correctionLabel = when {
            phraseCorrection != null && phraseCorrection != normalized ->
                "Phrase match: $phraseCorrection"
            correctedText != input.trim() ->
                "Auto-corrected to $correctedText"
            else ->
                "No change needed"
        }

        val confidence = when {
            phraseCorrection != null && phraseCorrection == normalized -> "High confidence"
            phraseCorrection != null -> "Phrase learned or known"
            correctedText != input.trim() -> "Local autocorrect"
            else -> "Typing looks familiar"
        }

        val predictions = buildPredictions(normalized, correctedText)
        val nextWordSuggestions = nextWordSuggestions(normalized)
        val nextPhraseSuggestions = nextPhraseSuggestions(normalized)

        return SmartLearningPreview(
            correctedText = correctedText,
            correctionLabel = correctionLabel,
            confidence = confidence,
            predictions = predictions,
            nextWordSuggestions = nextWordSuggestions,
            nextPhraseSuggestions = nextPhraseSuggestions,
            recentPhrases = recentPhrases.take(MAX_RECENT),
            recentWords = recentWords.take(MAX_RECENT),
            learnedSessions = stats.learnedSessions,
            uniqueWords = stats.uniqueWords,
            uniquePhrases = stats.uniquePhrases
        )
    }

    fun learnPhrase(input: String) {
        val normalized = normalize(input)
        if (normalized.isBlank()) return

        val tokens = normalized.split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return

        learnedSessions += 1
        phraseCounts[normalized] = (phraseCounts[normalized] ?: 0) + 1
        rememberRecentPhrase(normalized)

        tokens.forEach { token ->
            wordCounts[token] = (wordCounts[token] ?: 0) + 1
            rememberRecentWord(token)
        }

        tokens.zipWithNext().forEach { (left, right) ->
            val key = "$left|$right"
            bigramCounts[key] = (bigramCounts[key] ?: 0) + 1
        }

        saveState()
    }

    fun reset() {
        learnedSessions = 0
        wordCounts.clear()
        phraseCounts.clear()
        bigramCounts.clear()
        recentPhrases.clear()
        recentWords.clear()
        saveState()
    }

    fun stats(): SmartLearningStats {
        return SmartLearningStats(
            learnedSessions = learnedSessions,
            uniqueWords = wordCounts.size,
            uniquePhrases = phraseCounts.size,
            uniqueBigrams = bigramCounts.size
        )
    }

    fun history(limit: Int = MAX_RECENT): SmartLearningHistory {
        return SmartLearningHistory(
            recentPhrases = recentPhrases.take(limit),
            recentWords = recentWords.take(limit)
        )
    }

    private fun buildPredictions(normalizedInput: String, correctedText: String): List<String> {
        val source = if (correctedText.isNotBlank()) normalize(correctedText) else normalizedInput
        val tokens = source.split(" ").filter { it.isNotBlank() }
        val previousWord = tokens.lastOrNull().orEmpty()
        val prefix = if (normalizedInput.endsWith(" ")) "" else tokens.lastOrNull().orEmpty()

        val phraseSuggestions = phraseCounts.keys
            .asSequence()
            .filter { it.startsWith(source) }
            .sortedWith(
                compareByDescending<String> { phraseCounts[it] ?: 0 }
                    .thenBy { it.length }
            )
            .take(3)
            .toList()

        if (phraseSuggestions.isNotEmpty()) {
            return phraseSuggestions
        }

        val bigramSuggestions = bigramCounts.entries
            .asSequence()
            .filter { (key, _) -> previousWord.isNotBlank() && key.startsWith("$previousWord|") }
            .map { (key, count) ->
                key.substringAfter('|') to count
            }
            .sortedWith(
                compareByDescending<Pair<String, Int>> { it.second }
                    .thenBy { it.first.length }
            )
            .map { it.first }
            .take(4)
            .toList()

        if (bigramSuggestions.isNotEmpty()) {
            return bigramSuggestions
        }

        val lexicalSuggestions = lexicalEntries
            .asSequence()
            .filter { prefix.isBlank() || it.text.startsWith(prefix) }
            .sortedByDescending { it.weight }
            .map { it.text }
            .take(4)
            .toList()

        if (lexicalSuggestions.isNotEmpty()) {
            return lexicalSuggestions
        }

        val learningPool = buildSet {
            addAll(AmharicTranslator.knownEnglishVocabulary())
            addAll(wordCounts.keys)
        }

        return learningPool
            .asSequence()
            .filter { prefix.isBlank() || it.startsWith(prefix) || it.contains(' ') }
            .sortedWith(
                compareByDescending<String> { wordCounts[it] ?: 0 }
                    .thenBy { baseRankFor(it) }
                    .thenBy { it.length }
            )
            .take(4)
            .toList()
    }

    private fun nextWordSuggestions(normalizedInput: String): List<String> {
        val tokens = normalizedInput.split(" ").filter { it.isNotBlank() }
        val previousWord = tokens.lastOrNull().orEmpty()
        if (previousWord.isBlank()) return emptyList()

        return bigramCounts.entries
            .asSequence()
            .filter { (key, _) -> key.startsWith("$previousWord|") }
            .map { (key, count) ->
                key.substringAfter('|') to count
            }
            .sortedWith(
                compareByDescending<Pair<String, Int>> { it.second }
                    .thenBy { it.first.length }
            )
            .map { it.first }
            .take(5)
            .toList()
    }

    private fun nextPhraseSuggestions(normalizedInput: String): List<String> {
        if (normalizedInput.isBlank()) {
            return topPhrases(5)
        }

        return phraseCounts.keys
            .asSequence()
            .filter { it.startsWith(normalizedInput) && it != normalizedInput }
            .sortedWith(
                compareByDescending<String> { phraseCounts[it] ?: 0 }
                    .thenBy { it.length }
            )
            .take(5)
            .toList()
    }

    private fun topPhrases(limit: Int): List<String> {
        return phraseCounts.entries
            .asSequence()
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { it.key.length }
            )
            .map { it.key }
            .take(limit)
            .toList()
            .ifEmpty {
                AmharicTranslator.knownEnglishPhrases()
                    .asSequence()
                    .sortedBy { it.length }
                    .take(limit)
                    .toList()
            }
    }

    private fun bestPhraseMatch(normalizedInput: String): String? {
        val candidates = buildSet {
            addAll(AmharicTranslator.knownEnglishPhrases())
            addAll(phraseCounts.keys)
        }

        if (candidates.contains(normalizedInput)) {
            return normalizedInput
        }

        return candidates
            .asSequence()
            .map { candidate ->
                candidate to editDistance(normalizedInput, candidate)
            }
            .filter { (_, distance) ->
                distance <= max(2, normalizedInput.length / 4)
            }
            .sortedWith(
                compareBy<Pair<String, Int>> { it.second }
                    .thenByDescending { phraseCounts[it.first] ?: 0 }
                    .thenBy { baseRankFor(it.first) }
                    .thenBy { it.first.length }
            )
            .firstOrNull()
            ?.first
    }

    private fun autocorrectByToken(input: String): String {
        val trimmed = input.trim()
        val endsWithSpace = input.lastOrNull()?.isWhitespace() == true
        val tokens = normalize(trimmed).split(" ").filter { it.isNotBlank() }.toMutableList()
        if (tokens.isEmpty()) return trimmed

        if (endsWithSpace) {
            return tokens.joinToString(" ")
        }

        val lastIndex = tokens.lastIndex
        val token = tokens[lastIndex]
        val corrected = bestWordMatch(token)
        if (corrected != null) {
            tokens[lastIndex] = corrected
        }

        return tokens.joinToString(" ")
    }

    private fun bestWordMatch(token: String): String? {
        val candidates = buildSet {
            addAll(AmharicTranslator.knownEnglishVocabulary().filter { it.isNotBlank() && !it.contains(' ') })
            addAll(wordCounts.keys)
        }

        if (candidates.contains(token)) {
            return token
        }

        val ranked = candidates
            .asSequence()
            .map { candidate ->
                candidate to editDistance(token, candidate)
            }
            .filter { (_, distance) ->
                distance <= max(2, token.length / 3)
            }
            .sortedWith(
                compareBy<Pair<String, Int>> { it.second }
                    .thenByDescending { wordCounts[it.first] ?: 0 }
                    .thenBy { baseRankFor(it.first) }
                    .thenBy { it.first.length }
            )
            .toList()

        return ranked.firstOrNull()?.first
    }

    private fun baseRankFor(word: String): Int {
        return when {
            AmharicTranslator.knownEnglishPhrases().contains(word) -> 0
            AmharicTranslator.knownEnglishVocabulary().contains(word) -> 1
            else -> 2
        }
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9\\s']"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun editDistance(left: String, right: String): Int {
        if (left == right) return 0
        if (left.isEmpty()) return right.length
        if (right.isEmpty()) return left.length

        val previous = IntArray(right.length + 1) { it }
        val current = IntArray(right.length + 1)

        for (i in left.indices) {
            current[0] = i + 1
            for (j in right.indices) {
                val cost = if (left[i] == right[j]) 0 else 1
                current[j + 1] = minOf(
                    previous[j + 1] + 1,
                    current[j] + 1,
                    previous[j] + cost
                )
            }

            for (j in previous.indices) {
                previous[j] = current[j]
            }
        }

        return previous[right.length]
    }

    private fun loadState() {
        learnedSessions = prefs.getInt(KEY_LEARNED_SESSIONS, 0)
        wordCounts.putAll(readMap(KEY_WORD_COUNTS))
        phraseCounts.putAll(readMap(KEY_PHRASE_COUNTS))
        bigramCounts.putAll(readMap(KEY_BIGRAM_COUNTS))
        recentPhrases.clear()
        recentPhrases.addAll(readList(KEY_RECENT_PHRASES))
        recentWords.clear()
        recentWords.addAll(readList(KEY_RECENT_WORDS))
    }

    private fun saveState() {
        prefs.edit()
            .putInt(KEY_LEARNED_SESSIONS, learnedSessions)
            .putString(KEY_WORD_COUNTS, writeMap(wordCounts))
            .putString(KEY_PHRASE_COUNTS, writeMap(phraseCounts))
            .putString(KEY_BIGRAM_COUNTS, writeMap(bigramCounts))
            .putString(KEY_RECENT_PHRASES, writeList(recentPhrases))
            .putString(KEY_RECENT_WORDS, writeList(recentWords))
            .apply()
    }

    private fun readMap(key: String): MutableMap<String, Int> {
        val raw = prefs.getString(key, null).orEmpty()
        if (raw.isBlank()) return mutableMapOf()

        return try {
            val json = JSONObject(raw)
            buildMap {
                json.keys().forEach { entryKey ->
                    put(entryKey, json.optInt(entryKey, 0))
                }
            }.toMutableMap()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun readList(key: String): MutableList<String> {
        val raw = prefs.getString(key, null).orEmpty()
        if (raw.isBlank()) return mutableListOf()

        return try {
            val json = JSONArray(raw)
            val list = mutableListOf<String>()
            for (index in 0 until json.length()) {
                list.add(json.optString(index, ""))
            }
            list.filter { it.isNotBlank() }.toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun writeList(values: List<String>): String {
        val json = JSONArray()
        values.forEach { value ->
            json.put(value)
        }
        return json.toString()
    }

    private fun rememberRecentPhrase(value: String) {
        rememberRecent(value, recentPhrases)
    }

    private fun rememberRecentWord(value: String) {
        rememberRecent(value, recentWords)
    }

    private fun rememberRecent(value: String, target: MutableList<String>) {
        val normalized = value.trim()
        if (normalized.isBlank()) return
        target.remove(normalized)
        target.add(0, normalized)
        if (target.size > MAX_RECENT) {
            target.subList(MAX_RECENT, target.size).clear()
        }
    }

    private fun writeMap(values: Map<String, Int>): String {
        val json = JSONObject()
        values.forEach { (key, value) ->
            json.put(key, value)
        }
        return json.toString()
    }

    companion object {
        private const val PREFS_NAME = "smart_learning_engine"
        private const val KEY_LEARNED_SESSIONS = "learned_sessions"
        private const val KEY_WORD_COUNTS = "word_counts"
        private const val KEY_PHRASE_COUNTS = "phrase_counts"
        private const val KEY_BIGRAM_COUNTS = "bigram_counts"
        private const val KEY_RECENT_PHRASES = "recent_phrases"
        private const val KEY_RECENT_WORDS = "recent_words"
        private const val MAX_RECENT = 8
    }
}
