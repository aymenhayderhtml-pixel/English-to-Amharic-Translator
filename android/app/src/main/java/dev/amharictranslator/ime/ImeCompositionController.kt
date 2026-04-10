package dev.amharictranslator.ime

import dev.amharictranslator.data.AmharicTranslator
import dev.amharictranslator.data.DictionaryData
import dev.amharictranslator.keyboard.CommitTrigger

data class ImeCommitResult(
    val latinText: String,
    val amharicText: String,
    val trigger: CommitTrigger
)

/**
 * Pure text buffer for the system keyboard.
 *
 * The IME owns the surrounding InputConnection. This controller only keeps the
 * current Latin composition and turns it into Amharic when a commit trigger is
 * pressed.
 */
class ImeCompositionController(
    private val dictionary: DictionaryData
) {
    private val buffer = StringBuilder()

    fun reset() {
        buffer.clear()
    }

    fun hasComposition(): Boolean = buffer.isNotEmpty()

    fun currentLatinText(): String = buffer.toString()

    fun previewText(): String {
        val latin = buffer.toString()
        if (latin.isBlank()) return ""
        return AmharicTranslator.previewForSuggestion(latin, dictionary)
    }

    fun inputLatin(char: Char): String {
        buffer.append(char.lowercaseChar())
        return previewText()
    }

    fun deleteBackward(): String {
        if (buffer.isNotEmpty()) {
            buffer.deleteCharAt(buffer.lastIndex)
        }
        return previewText()
    }

    fun commit(trigger: CommitTrigger): ImeCommitResult? {
        val latin = buffer.toString()
        if (latin.isBlank()) {
            return null
        }

        val amharic = AmharicTranslator.transliterateWord(latin, dictionary).text
        buffer.clear()
        return ImeCommitResult(
            latinText = latin,
            amharicText = amharic,
            trigger = trigger
        )
    }
}
