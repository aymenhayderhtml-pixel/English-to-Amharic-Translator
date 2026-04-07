package dev.amharictranslator.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

@Entity(tableName = "learned_word_counts")
data class LearnedWordCountEntity(
    @PrimaryKey val word: String,
    val count: Int
)

@Entity(tableName = "learned_phrase_counts")
data class LearnedPhraseCountEntity(
    @PrimaryKey val phrase: String,
    val count: Int
)

@Entity(tableName = "learned_bigrams", primaryKeys = ["previousWord", "nextWord"])
data class LearnedBigramCountEntity(
    val previousWord: String,
    val nextWord: String,
    val count: Int
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Dao
interface LearningDao {
    @Query("SELECT * FROM learned_word_counts")
    suspend fun getWordCounts(): List<LearnedWordCountEntity>

    @Query("SELECT * FROM learned_phrase_counts")
    suspend fun getPhraseCounts(): List<LearnedPhraseCountEntity>

    @Query("SELECT * FROM learned_bigrams")
    suspend fun getBigramCounts(): List<LearnedBigramCountEntity>

    @Query("SELECT count FROM learned_word_counts WHERE word = :word LIMIT 1")
    suspend fun getWordCount(word: String): Int?

    @Query("SELECT count FROM learned_phrase_counts WHERE phrase = :phrase LIMIT 1")
    suspend fun getPhraseCount(phrase: String): Int?

    @Query("SELECT count FROM learned_bigrams WHERE previousWord = :previousWord AND nextWord = :nextWord LIMIT 1")
    suspend fun getBigramCount(previousWord: String, nextWord: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWordCount(entity: LearnedWordCountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhraseCount(entity: LearnedPhraseCountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBigramCount(entity: LearnedBigramCountEntity)

    @Query("DELETE FROM learned_word_counts WHERE word NOT IN (SELECT word FROM learned_word_counts ORDER BY count DESC, word ASC LIMIT :limit)")
    suspend fun trimWords(limit: Int)

    @Query("DELETE FROM learned_phrase_counts WHERE phrase NOT IN (SELECT phrase FROM learned_phrase_counts ORDER BY count DESC, phrase ASC LIMIT :limit)")
    suspend fun trimPhrases(limit: Int)

    @Query("DELETE FROM learned_bigrams WHERE rowid NOT IN (SELECT rowid FROM learned_bigrams ORDER BY count DESC, previousWord ASC, nextWord ASC LIMIT :limit)")
    suspend fun trimBigrams(limit: Int)

    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetting(setting: AppSettingEntity)

    @Query("DELETE FROM app_settings WHERE key IN (:keys)")
    suspend fun deleteSettings(keys: List<String>)

    @Query("DELETE FROM learned_word_counts")
    suspend fun clearWords()

    @Query("DELETE FROM learned_phrase_counts")
    suspend fun clearPhrases()

    @Query("DELETE FROM learned_bigrams")
    suspend fun clearBigrams()
}

@Database(
    entities = [
        LearnedWordCountEntity::class,
        LearnedPhraseCountEntity::class,
        LearnedBigramCountEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LearningDatabase : RoomDatabase() {
    abstract fun learningDao(): LearningDao

    companion object {
        @Volatile
        private var instance: LearningDatabase? = null

        fun getInstance(context: Context): LearningDatabase {
            instance?.let { return it }

            return synchronized(this) {
                instance?.let { return@synchronized it }

                val database = Room.databaseBuilder(
                    context.applicationContext,
                    LearningDatabase::class.java,
                    "learning.db"
                ).build()

                instance = database
                database
            }
        }

        fun createInMemory(context: Context): LearningDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                LearningDatabase::class.java
            ).allowMainThreadQueries().build()
        }
    }
}

data class LearningCache(
    val wordCounts: Map<String, Int> = emptyMap(),
    val phraseCounts: Map<String, Int> = emptyMap(),
    val bigramCounts: Map<String, Int> = emptyMap(),
    val recentWords: List<String> = emptyList(),
    val recentPhrases: List<String> = emptyList(),
    val learnedSessions: Int = 0
) {
    val uniqueWords: Int
        get() = wordCounts.size

    val uniquePhrases: Int
        get() = phraseCounts.size
}

object LearningSettings {
    const val KEYBOARD_DRAFT = "keyboard_draft"
    const val KEYBOARD_CURSOR = "keyboard_cursor"
    const val TRANSLATOR_DRAFT = "translator_draft"
    const val LAST_COMMITTED_WORD = "last_committed_word"
    const val LEARNED_SESSIONS = "learned_sessions"
    const val RECENT_WORDS = "recent_words"
    const val RECENT_PHRASES = "recent_phrases"
}

class LearningRepository private constructor(
    private val dao: LearningDao
) {
    suspend fun loadCache(): LearningCache = withContext(Dispatchers.IO) {
        LearningCache(
            wordCounts = dao.getWordCounts().associate { it.word to it.count },
            phraseCounts = dao.getPhraseCounts().associate { it.phrase to it.count },
            bigramCounts = dao.getBigramCounts().associate { "${it.previousWord}|${it.nextWord}" to it.count },
            recentWords = readList(LearningSettings.RECENT_WORDS),
            recentPhrases = readList(LearningSettings.RECENT_PHRASES),
            learnedSessions = readInt(LearningSettings.LEARNED_SESSIONS)
        )
    }

    suspend fun recordCommittedWord(word: String, previousWord: String?) = withContext(Dispatchers.IO) {
        val normalizedWord = AmharicTranslator.normalizeWord(word)
        val normalizedPrevious = previousWord?.let(AmharicTranslator::normalizeWord).orEmpty()
        if (normalizedWord.isBlank()) return@withContext

        incrementWord(normalizedWord)
        if (normalizedPrevious.isNotBlank()) {
            incrementBigram(normalizedPrevious, normalizedWord)
        }

        incrementSessions()
        writeRecentWord(normalizedWord)
        trimTables()
    }

    suspend fun recordAcceptedPhrase(phrase: String) = withContext(Dispatchers.IO) {
        val normalizedPhrase = AmharicTranslator.normalizeEnglish(phrase)
        if (normalizedPhrase.isBlank()) return@withContext

        incrementPhrase(normalizedPhrase)

        val tokens = normalizedPhrase.split(" ").filter { it.isNotBlank() }
        tokens.forEach { incrementWord(it) }
        tokens.zipWithNext().forEach { (left, right) ->
            incrementBigram(left, right)
        }

        incrementSessions()
        writeRecentPhrase(normalizedPhrase)
        trimTables()
    }

    suspend fun getSetting(key: String, defaultValue: String = ""): String = withContext(Dispatchers.IO) {
        dao.getSetting(key).orEmpty().ifBlank { defaultValue }
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.upsertSetting(AppSettingEntity(key = key, value = value))
    }

    suspend fun resetLearning() = withContext(Dispatchers.IO) {
        dao.clearWords()
        dao.clearPhrases()
        dao.clearBigrams()
        dao.deleteSettings(
            listOf(
                LearningSettings.LEARNED_SESSIONS,
                LearningSettings.RECENT_WORDS,
                LearningSettings.RECENT_PHRASES,
                LearningSettings.LAST_COMMITTED_WORD
            )
        )
    }

    private suspend fun incrementSessions() {
        val nextValue = readInt(LearningSettings.LEARNED_SESSIONS) + 1
        dao.upsertSetting(AppSettingEntity(LearningSettings.LEARNED_SESSIONS, nextValue.toString()))
    }

    private suspend fun trimTables() {
        dao.trimWords(MAX_WORDS)
        dao.trimPhrases(MAX_PHRASES)
        dao.trimBigrams(MAX_BIGRAMS)
    }

    private suspend fun incrementWord(word: String) {
        val count = (dao.getWordCount(word) ?: 0) + 1
        dao.upsertWordCount(LearnedWordCountEntity(word, count))
    }

    private suspend fun incrementPhrase(phrase: String) {
        val count = (dao.getPhraseCount(phrase) ?: 0) + 1
        dao.upsertPhraseCount(LearnedPhraseCountEntity(phrase, count))
    }

    private suspend fun incrementBigram(previousWord: String, nextWord: String) {
        val count = (dao.getBigramCount(previousWord, nextWord) ?: 0) + 1
        dao.upsertBigramCount(LearnedBigramCountEntity(previousWord, nextWord, count))
    }

    private suspend fun writeRecentWord(word: String) {
        writeList(LearningSettings.RECENT_WORDS, updateRecentList(readList(LearningSettings.RECENT_WORDS), word))
    }

    private suspend fun writeRecentPhrase(phrase: String) {
        writeList(LearningSettings.RECENT_PHRASES, updateRecentList(readList(LearningSettings.RECENT_PHRASES), phrase))
    }

    private suspend fun readInt(key: String): Int {
        return dao.getSetting(key)?.toIntOrNull() ?: 0
    }

    private suspend fun readList(key: String): List<String> {
        val raw = dao.getSetting(key).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val jsonArray = JSONArray(raw)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val value = jsonArray.optString(index).trim()
                    if (value.isNotBlank()) {
                        add(value)
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private suspend fun writeList(key: String, values: List<String>) {
        val jsonArray = JSONArray()
        values.forEach(jsonArray::put)
        dao.upsertSetting(AppSettingEntity(key, jsonArray.toString()))
    }

    private fun updateRecentList(existing: List<String>, value: String): List<String> {
        val normalized = value.trim()
        if (normalized.isBlank()) return existing

        val updated = existing.toMutableList()
        updated.remove(normalized)
        updated.add(0, normalized)
        if (updated.size > MAX_RECENT) {
            updated.subList(MAX_RECENT, updated.size).clear()
        }
        return updated
    }

    companion object {
        private const val MAX_WORDS = 5000
        private const val MAX_PHRASES = 2000
        private const val MAX_BIGRAMS = 10000
        private const val MAX_RECENT = 8

        fun create(context: Context): LearningRepository {
            return LearningRepository(LearningDatabase.getInstance(context).learningDao())
        }

        fun createInMemory(context: Context): LearningRepository {
            return LearningRepository(LearningDatabase.createInMemory(context).learningDao())
        }
    }
}
