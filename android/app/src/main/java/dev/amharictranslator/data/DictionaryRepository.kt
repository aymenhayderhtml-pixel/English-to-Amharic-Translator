package dev.amharictranslator.data

import android.content.Context
import org.json.JSONObject

data class SyllableRule(
    val latin: String,
    val amharic: String
)

data class DictionaryData(
    val phrasebook: Map<String, String>,
    val wordHints: Map<String, String>,
    val syllableRules: List<SyllableRule>
)

object DictionaryRepository {
    private const val DEFAULT_ASSET = "data/amharic_dictionary.json"

    @Volatile
    private var cached: DictionaryData? = null

    fun load(context: Context, assetPath: String = DEFAULT_ASSET): DictionaryData {
        cached?.let { return it }

        return synchronized(this) {
            cached?.let { return@synchronized it }

            val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val dictionary = parse(json)
            cached = dictionary
            dictionary
        }
    }

    private fun parse(rawJson: String): DictionaryData {
        val payload = JSONObject(rawJson)

        return DictionaryData(
            phrasebook = payload.getJSONObject("phrasebook").toStringMap(),
            wordHints = payload.getJSONObject("wordHints").toStringMap(),
            syllableRules = payload.getJSONArray("syllableRules").let { rules ->
                buildList {
                    for (index in 0 until rules.length()) {
                        val rule = rules.getJSONObject(index)
                        add(
                            SyllableRule(
                                latin = rule.getString("latin"),
                                amharic = rule.getString("amharic")
                            )
                        )
                    }
                }
            }
        )
    }

    private fun JSONObject.toStringMap(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        val iterator = keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = getString(key)
        }
        return map
    }
}
