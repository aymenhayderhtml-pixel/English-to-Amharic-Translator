package dev.amharictranslator.keyboard

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import dev.amharictranslator.data.DictionaryData
import dev.amharictranslator.data.SyllableRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyboardEngineTest {
    private val dictionary = DictionaryData(
        phrasebook = linkedMapOf("hello" to "ሰላም"),
        wordHints = linkedMapOf(
            "selam" to "ሰላም",
            "market" to "ገበያ",
            "home" to "ቤት"
        ),
        syllableRules = listOf(
            SyllableRule("hee", "ሄ"),
            SyllableRule("he", "ሀ"),
            SyllableRule("hu", "ሁ"),
            SyllableRule("hi", "ሂ"),
            SyllableRule("ha", "ሃ"),
            SyllableRule("h", "ህ")
        )
    )

    private val engine = KeyboardEngine(dictionary)

    @Test
    fun `space commits transliterated token`() {
        val previous = TextFieldValue("he", TextRange(2))
        val next = TextFieldValue("he ", TextRange(3))

        val result = engine.processEdit(previous, next)

        assertEquals("ሀ ", result.value.text)
        assertEquals("he", result.committedLatinToken)
        assertEquals("", result.currentToken)
    }

    @Test
    fun `exact dictionary word wins before fallback`() {
        val previous = TextFieldValue("selam", TextRange(5))
        val next = TextFieldValue("selam ", TextRange(6))

        val result = engine.processEdit(previous, next)

        assertEquals("ሰላም ", result.value.text)
        assertEquals("selam", result.committedLatinToken)
    }

    @Test
    fun `punctuation keeps separator while committing token`() {
        val previous = TextFieldValue("market", TextRange(6))
        val next = TextFieldValue("market,", TextRange(7))

        val result = engine.processEdit(previous, next)

        assertEquals("ገበያ,", result.value.text)
        assertEquals("market", result.committedLatinToken)
    }

    @Test
    fun `cursor edit in middle commits only token before cursor`() {
        val baseText = "ሰላም market home"
        val cursor = "ሰላም market".length
        val previous = TextFieldValue(baseText, TextRange(cursor))
        val next = TextFieldValue("ሰላም market, home", TextRange(cursor + 1))

        val result = engine.processEdit(previous, next)

        assertEquals("ሰላም ገበያ, home", result.value.text)
        assertEquals("market", result.committedLatinToken)
    }

    @Test
    fun `backspace after committed text deletes normally`() {
        val previous = TextFieldValue("ሰላም ", TextRange(5))
        val next = TextFieldValue("ሰላም", TextRange(4))

        val result = engine.processEdit(previous, next)

        assertEquals("ሰላም", result.value.text)
        assertNull(result.committedLatinToken)
    }

    @Test
    fun `mixed input without trailing latin token stays unchanged`() {
        val result = engine.commitLastToken(
            text = "abc123 ",
            cursor = 7,
            trigger = CommitTrigger.Space
        )

        assertEquals("abc123 ", result.value.text)
        assertNull(result.committedLatinToken)
    }
}
