package dev.amharictranslator.data

import android.content.Context
import org.json.JSONObject

data class SyllableRule(
    val latin: String,
    val amharic: String
)

data class DictionaryData(
    val phrasebook: LinkedHashMap<String, String>,
    val wordHints: LinkedHashMap<String, String>,
    val syllableRules: List<SyllableRule>
)

object DictionaryRepository {
    private const val DEFAULT_ASSET = "data/amharic_dictionary.json"

    @Volatile
    private var cachedDictionary: DictionaryData? = null

    fun load(context: Context, assetPath: String = DEFAULT_ASSET): DictionaryData {
        cachedDictionary?.let { return it }

        return synchronized(this) {
            cachedDictionary?.let { return@synchronized it }

            val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val dictionary = parse(json)
            cachedDictionary = dictionary
            dictionary
        }
    }

    private fun parse(rawJson: String): DictionaryData {
        val payload = JSONObject(rawJson)
        val phrasebook = payload.getJSONObject("phrasebook").toOrderedMap()
        val wordHints = payload.getJSONObject("wordHints").toOrderedMap()
        val syllableRules = payload.getJSONArray("syllableRules").let { array ->
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        SyllableRule(
                            latin = item.getString("latin"),
                            amharic = item.getString("amharic")
                        )
                    )
                }
            }
        }

        return DictionaryData(
            phrasebook = phrasebook,
            wordHints = wordHints,
            syllableRules = syllableRules
        )
    }

    private fun JSONObject.toOrderedMap(): LinkedHashMap<String, String> {
        val orderedMap = LinkedHashMap<String, String>()
        val iterator = keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            orderedMap[key] = getString(key)
        }
        return orderedMap
    }
}
