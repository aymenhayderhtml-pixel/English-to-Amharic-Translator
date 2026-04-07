package dev.amharictranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dev.amharictranslator.data.DictionaryRepository
import dev.amharictranslator.data.LearningRepository
import dev.amharictranslator.ui.screens.HomeScreen
import dev.amharictranslator.ui.theme.AmharicTranslatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContext = applicationContext
        val dictionary = DictionaryRepository.load(appContext)
        val learningRepository = LearningRepository.create(appContext)

        setContent {
            AmharicTranslatorTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    HomeScreen(
                        dictionary = dictionary,
                        learningRepository = learningRepository
                    )
                }
            }
        }
    }
}
