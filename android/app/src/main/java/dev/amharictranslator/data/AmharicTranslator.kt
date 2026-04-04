package dev.amharictranslator.data

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

object AmharicTranslator {
    private val phrasebook = linkedMapOf(
        "hello" to "ሰላም",
        "hi" to "ሰላም",
        "good morning" to "እንደምን አደርክ",
        "good afternoon" to "እንደምን አለህ",
        "good evening" to "እንደምን አመሸህ",
        "good night" to "መልካም ምሽት",
        "thank you" to "አመሰግናለሁ",
        "please" to "እባክህ",
        "yes" to "አዎ",
        "no" to "አይ",
        "how are you" to "እንዴት ነህ",
        "what is your name" to "ስምህ ማን ነው",
        "my name is" to "ስሜ",
        "welcome" to "እንኳን ደህና መጣህ",
        "sorry" to "ይቅርታ",
        "i love you" to "እወድሃለሁ",
        "water" to "ውሃ",
        "food" to "ምግብ",
        "coffee" to "ቡና",
        "tea" to "ሻይ",
        "bread" to "ዳቦ",
        "home" to "ቤት",
        "school" to "ትምህርት ቤት",
        "help" to "እርዳታ",
        "where is the market" to "ገበያው የት ነው",
        "today" to "ዛሬ",
        "tomorrow" to "ነገ",
        "yesterday" to "ትናንት",
        "i am happy" to "ደስ ብሎኛል"
    )

    private val wordHints = linkedMapOf(
        "amharic" to "አማርኛ",
        "ethiopia" to "ኢትዮጵያ",
        "ethiopian" to "ኢትዮጵያዊ",
        "addis" to "አዲስ",
        "ababa" to "አበባ",
        "selam" to "ሰላም",
        "coffee" to "ቡና",
        "water" to "ውሃ",
        "bread" to "ዳቦ",
        "food" to "ምግብ",
        "school" to "ትምህርት ቤት",
        "market" to "ገበያ",
        "home" to "ቤት",
        "today" to "ዛሬ",
        "tomorrow" to "ነገ",
        "yesterday" to "ትናንት",
        "help" to "እርዳታ",
        "friend" to "ጓደኛ",
        "city" to "ከተማ",
        "name" to "ስም",
        "i" to "እኔ",
        "you" to "አንተ",
        "we" to "እኛ",
        "they" to "እነርሱ",
        "and" to "እና",
        "or" to "ወይም",
        "with" to "ጋር",
        "for" to "ለ",
        "in" to "ውስጥ",
        "to" to "ወደ",
        "is" to "ነው",
        "are" to "ናቸው",
        "my" to "የኔ",
        "your" to "የአንተ"
    )

    private val syllableRules = listOf(
        "hue" to "ኃ",
        "hui" to "ኅ",
        "hua" to "ኋ",
        "hee" to "ሄ",
        "shee" to "ሼ",
        "chee" to "ቼ",
        "qee" to "ቄ",
        "gee" to "ጌ",
        "kee" to "ኬ",
        "mee" to "ሜ",
        "nee" to "ኔ",
        "ree" to "ሬ",
        "see" to "ሴ",
        "tee" to "ቴ",
        "dee" to "ዴ",
        "lee" to "ሌ",
        "bee" to "ቤ",
        "fee" to "ፌ",
        "pee" to "ፔ",
        "vee" to "ቬ",
        "wee" to "ዌ",
        "yee" to "ዬ",
        "zee" to "ዜ",
        "jee" to "ጄ",
        "xee" to "ኼ",
        "he" to "ሀ",
        "hu" to "ሁ",
        "hi" to "ሂ",
        "ha" to "ሃ",
        "ho" to "ሆ",
        "h" to "ህ",
        "she" to "ሸ",
        "shu" to "ሹ",
        "shi" to "ሺ",
        "sha" to "ሻ",
        "sh" to "ሽ",
        "sho" to "ሾ",
        "che" to "ቸ",
        "chu" to "ቹ",
        "chi" to "ቺ",
        "cha" to "ቻ",
        "ch" to "ች",
        "cho" to "ቾ",
        "qe" to "ቀ",
        "qu" to "ቁ",
        "qi" to "ቂ",
        "qa" to "ቃ",
        "q" to "ቅ",
        "qo" to "ቆ",
        "ge" to "ገ",
        "gu" to "ጉ",
        "gi" to "ጊ",
        "ga" to "ጋ",
        "g" to "ግ",
        "go" to "ጎ",
        "ke" to "ከ",
        "ku" to "ኩ",
        "ki" to "ኪ",
        "ka" to "ካ",
        "k" to "ክ",
        "ko" to "ኮ",
        "me" to "መ",
        "mu" to "ሙ",
        "mi" to "ሚ",
        "ma" to "ማ",
        "m" to "ም",
        "mo" to "ሞ",
        "ne" to "ነ",
        "nu" to "ኑ",
        "ni" to "ኒ",
        "na" to "ና",
        "n" to "ን",
        "no" to "ኖ",
        "re" to "ረ",
        "ru" to "ሩ",
        "ri" to "ሪ",
        "ra" to "ራ",
        "r" to "ር",
        "ro" to "ሮ",
        "se" to "ሰ",
        "su" to "ሱ",
        "si" to "ሲ",
        "sa" to "ሳ",
        "s" to "ስ",
        "so" to "ሶ",
        "te" to "ተ",
        "tu" to "ቱ",
        "ti" to "ቲ",
        "ta" to "ታ",
        "t" to "ት",
        "to" to "ቶ",
        "de" to "ደ",
        "du" to "ዱ",
        "di" to "ዲ",
        "da" to "ዳ",
        "d" to "ድ",
        "do" to "ዶ",
        "le" to "ለ",
        "lu" to "ሉ",
        "li" to "ሊ",
        "la" to "ላ",
        "l" to "ል",
        "lo" to "ሎ",
        "be" to "በ",
        "bu" to "ቡ",
        "bi" to "ቢ",
        "ba" to "ባ",
        "bua" to "ቧ",
        "b" to "ብ",
        "bo" to "ቦ",
        "fe" to "ፈ",
        "fu" to "ፉ",
        "fi" to "ፊ",
        "fa" to "ፋ",
        "f" to "ፍ",
        "fo" to "ፎ",
        "pe" to "ፐ",
        "pu" to "ፑ",
        "pi" to "ፒ",
        "pa" to "ፓ",
        "p" to "ፕ",
        "po" to "ፖ",
        "ve" to "ቨ",
        "vu" to "ቩ",
        "vi" to "ቪ",
        "va" to "ቫ",
        "v" to "ቭ",
        "vo" to "ቮ",
        "we" to "ወ",
        "wu" to "ዉ",
        "wi" to "ዊ",
        "wa" to "ዋ",
        "w" to "ው",
        "wo" to "ዎ",
        "ye" to "የ",
        "yu" to "ዩ",
        "yi" to "ዪ",
        "ya" to "ያ",
        "y" to "ይ",
        "yo" to "ዮ",
        "ze" to "ዘ",
        "zu" to "ዙ",
        "zi" to "ዚ",
        "za" to "ዛ",
        "z" to "ዝ",
        "zo" to "ዞ",
        "je" to "ጀ",
        "ju" to "ጁ",
        "ji" to "ጂ",
        "ja" to "ጃ",
        "j" to "ጅ",
        "jo" to "ጆ",
        "xe" to "ኸ",
        "xu" to "ኹ",
        "xi" to "ኺ",
        "xa" to "ኻ",
        "x" to "ኽ",
        "xo" to "ኾ",
        "aee" to "ኤ",
        "ae" to "አ",
        "au" to "ኡ",
        "ai" to "ኢ",
        "aa" to "ኣ",
        "ao" to "ኦ",
        "a" to "እ"
    )

    private val tokenRegex = Regex("[A-Za-z']+|[^A-Za-z']+")
    private val normalizeRegex = Regex("[^A-Za-z0-9\\s']")

    fun translate(input: String): TranslationResult {
        val normalized = normalizePhrase(input)

        if (normalized.isBlank()) {
            return TranslationResult("", "Waiting for input", "Prototype", "Local data")
        }

        phrasebook[normalized]?.let { exact ->
            return TranslationResult(
                output = exact,
                mode = "Phrasebook match",
                confidence = "High confidence",
                source = "Local phrasebook"
            )
        }

        return TranslationResult(
            output = transliterate(input),
            mode = "Transliteration fallback",
            confidence = "Prototype preview",
            source = "Local fallback"
        )
    }

    fun transliterate(input: String): String {
        if (input.isBlank()) return ""

        return tokenRegex.findAll(input)
            .joinToString(separator = "") { match ->
                val part = match.value
                if (part.matches(Regex("[A-Za-z']+"))) {
                    val lower = part.lowercase()
                    wordHints[lower] ?: transliterateWord(lower)
                } else {
                    part
                }
            }
            .trim()
    }

    fun suggestions(input: String): List<Suggestion> {
        val prefix = normalizePhrase(input)

        if (prefix.isBlank()) {
            return syllableRules.take(10).map { (latin, amharic) ->
                Suggestion(latin, amharic, "starter")
            }
        }

        val combined = buildList {
            addAll(
                phrasebook.entries
                    .filter { it.key.startsWith(prefix) }
                    .take(4)
                    .map { (latin, amharic) -> Suggestion(latin, amharic, "phrase") }
            )
            addAll(
                wordHints.entries
                    .filter { it.key.startsWith(prefix) }
                    .take(4)
                    .map { (latin, amharic) -> Suggestion(latin, amharic, "word") }
            )
            addAll(
                syllableRules
                    .filter { it.first.startsWith(prefix) }
                    .take(6)
                    .map { (latin, amharic) -> Suggestion(latin, amharic, "syllable") }
            )
        }

        return combined.distinctBy { "${it.latin}:${it.amharic}" }
    }

    fun currentToken(input: String): String {
        return Regex("[A-Za-z']+")
            .findAll(input)
            .lastOrNull()
            ?.value
            .orEmpty()
    }

    fun knownEnglishVocabulary(): Set<String> {
        return buildSet {
            addAll(phrasebook.keys)
            addAll(wordHints.keys)
        }
    }

    fun knownEnglishPhrases(): Set<String> {
        return phrasebook.keys.toSet()
    }

    private fun normalizePhrase(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(normalizeRegex, "")
            .replace(Regex("\\s+"), " ")
    }

    private fun transliterateWord(word: String): String {
        val result = StringBuilder()
        var index = 0

        while (index < word.length) {
            var matched = false

            for ((latin, amharic) in syllableRules) {
                if (word.startsWith(latin, index)) {
                    result.append(amharic)
                    index += latin.length
                    matched = true
                    break
                }
            }

            if (!matched) {
                when (val current = word[index]) {
                    '\'' -> index += 1
                    '-' -> {
                        result.append(' ')
                        index += 1
                    }
                    else -> {
                        result.append(current)
                        index += 1
                    }
                }
            }
        }

        return result.toString()
    }
}
