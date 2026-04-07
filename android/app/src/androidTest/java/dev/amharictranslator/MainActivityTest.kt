package dev.amharictranslator

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun typingSpaceCommitsTheLastWordToAmharic() {
        composeRule.onNodeWithTag("keyboard_input").performTextClearance()
        composeRule.onNodeWithTag("keyboard_input").performTextInput("selam ")
        composeRule.onNodeWithTag("keyboard_input").assertTextContains("ሰላም ")
    }
}
