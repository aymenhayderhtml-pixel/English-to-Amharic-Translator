package dev.amharictranslator.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LearningRepositoryTest {
    private lateinit var repository: LearningRepository

    @Before
    fun setUp() = runTest {
        repository = LearningRepository.createInMemory(ApplicationProvider.getApplicationContext())
        repository.resetLearning()
    }

    @Test
    fun `recordCommittedWord increments words and bigrams`() = runTest {
        repository.recordCommittedWord("selam", previousWord = null)
        repository.recordCommittedWord("market", previousWord = "selam")

        val cache = repository.loadCache()

        assertEquals(1, cache.wordCounts["selam"])
        assertEquals(1, cache.wordCounts["market"])
        assertEquals(1, cache.bigramCounts["selam|market"])
        assertEquals(listOf("market", "selam"), cache.recentWords.take(2))
    }

    @Test
    fun `recordAcceptedPhrase stores phrase and session metadata`() = runTest {
        repository.recordAcceptedPhrase("good morning friend")

        val cache = repository.loadCache()

        assertEquals(1, cache.learnedSessions)
        assertEquals(1, cache.phraseCounts["good morning friend"])
        assertTrue(cache.wordCounts.keys.containsAll(listOf("good", "morning", "friend")))
        assertEquals(listOf("good morning friend"), cache.recentPhrases)
    }

    @Test
    fun `repository trims word table to configured limit`() = runTest {
        repeat(5001) { index ->
            repository.recordCommittedWord("word$index", previousWord = null)
        }

        val cache = repository.loadCache()

        assertTrue(cache.wordCounts.size <= 5000)
    }
}
