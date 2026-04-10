package dev.amharictranslator.ime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.amharictranslator.data.AmharicTranslator
import dev.amharictranslator.data.DictionaryData
import dev.amharictranslator.data.DictionaryRepository
import dev.amharictranslator.keyboard.CommitTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImeCompositionControllerTest {
    private lateinit var controller: ImeCompositionController
    private lateinit var dictionary: DictionaryData

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dictionary = DictionaryRepository.load(context)
        controller = ImeCompositionController(dictionary)
    }

    @Test
    fun `preview updates while typing romanized Amharic`() {
        val preview = "selam".fold("") { _, char ->
            controller.inputLatin(char)
        }

        assertEquals(preview, controller.previewText())
        assertTrue(controller.hasComposition())
    }

    @Test
    fun `space commits the current word and clears the buffer`() {
        "selam".forEach { controller.inputLatin(it) }

        val committed = controller.commit(CommitTrigger.Space)

        assertEquals("selam", committed?.latinText)
        assertEquals("ሰላም", committed?.amharicText)
        assertFalse(controller.hasComposition())
        assertEquals("", controller.previewText())
    }

    @Test
    fun `backspace removes the last buffered character`() {
        "selam".forEach { controller.inputLatin(it) }
        val afterDelete = controller.deleteBackward()

        assertEquals(AmharicTranslator.previewForSuggestion("sela", dictionary), controller.previewText())
        assertEquals(controller.previewText(), afterDelete)
    }
}
