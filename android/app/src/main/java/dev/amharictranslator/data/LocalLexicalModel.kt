package dev.amharictranslator.data

import android.content.Context
import org.json.JSONObject

data class LexicalModelEntry(
    val text: String,
    val weight: Int
)

data class LexicalModelMeta(
    val name: String,
    val version: String,
    val language: String,
    val description: String
)

object LocalLexicalModel {
    private const val DEFAULT_ASSET = "keyman/lexical_model_seed.json"

    fun loadMeta(context: Context, assetPath: String = DEFAULT_ASSET): LexicalModelMeta? {
        return runCatching {
            val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val payload = JSONObject(json)

            LexicalModelMeta(
                name = payload.optString("name", "local-lexical"),
                version = payload.optString("version", "0.1.0"),
                language = payload.optString("language", "am"),
                description = payload.optString("description", "Local offline lexical model.")
            )
        }.getOrNull()
    }

    fun loadEntries(context: Context, assetPath: String = DEFAULT_ASSET): List<LexicalModelEntry> {
        return runCatching {
            val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val payload = JSONObject(json)
            val entries = payload.optJSONArray("entries") ?: return emptyList()

            buildList {
                for (index in 0 until entries.length()) {
                    val entry = entries.optJSONObject(index) ?: continue
                    val text = entry.optString("text", "").trim()
                    val weight = entry.optInt("weight", 0)
                    if (text.isNotBlank()) {
                        add(LexicalModelEntry(text, weight))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}
