package dev.amharictranslator.keyman

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.io.File
import java.io.FileOutputStream

data class KeymanInstallResult(
    val success: Boolean,
    val message: String,
    val installedKeyboardCount: Int = 0,
    val currentKeyboardId: String = "",
    val activePackageAsset: String = ""
)

object KeymanPackageInstaller {
    private const val KEYMAN_ASSET_DIR = "keyman"
    private const val DEFAULT_PACKAGE_FILE = "amharic-transliteration.kmp"
    private const val DEFAULT_PACKAGE_ID = "amharic-transliteration"
    private const val DEFAULT_KEYBOARD_ID = "amharic-transliteration"
    private const val DEFAULT_KEYBOARD_NAME = "Amharic Transliteration Keyboard"
    private const val DEFAULT_LANGUAGE_ID = "am"
    private const val DEFAULT_LANGUAGE_NAME = "Amharic"
    private const val DEFAULT_KEYBOARD_VERSION = "1.0"

    fun ensureInstalled(context: Context): KeymanInstallResult {
        return runCatching {
            val managerClass = Class.forName("com.keyman.engine.KMManager")
            val keyboardClass = Class.forName("com.keyman.engine.data.Keyboard")
            val packageProcessorClass = Class.forName("com.keyman.engine.packages.PackageProcessor")

            val packageDir = invokeStringMethod(managerClass, "getPackagesDir")
                ?.let { File(it) }
                ?: return KeymanInstallResult(false, "Keyman packages directory unavailable.")

            if (!packageDir.exists()) {
                packageDir.mkdirs()
            }

            val kmpAssets = listKmpAssets(context)
            val chosenAsset = selectPreferredKmpAsset(kmpAssets)
                ?: return KeymanInstallResult(
                    success = false,
                    message = "No .kmp keyboard package found in assets/$KEYMAN_ASSET_DIR/.",
                    activePackageAsset = ""
                )

            val packageFile = copyAssetToCache(context, chosenAsset)
                ?: return KeymanInstallResult(false, "Missing Keyman package asset: $chosenAsset", activePackageAsset = chosenAsset)

            val processor = packageProcessorClass
                .getConstructor(File::class.java)
                .newInstance(packageDir)

            val processMethod = packageProcessorClass.methods.firstOrNull { candidate ->
                candidate.name == "processKMP" && candidate.parameterTypes.size == 3
            } ?: return KeymanInstallResult(false, "Keyman package processor unavailable.")

            val installResult = processMethod.invoke(processor, packageFile, packageDir, "")
            val installedCount = when (installResult) {
                is Collection<*> -> installResult.size
                is Array<*> -> installResult.size
                else -> 0
            }

            val keyboardTypeClass = Class.forName("com.keyman.engine.KMManager\$KeyboardType")
            val inAppKeyboardType = enumValueOfClass(keyboardTypeClass, "KEYBOARD_TYPE_INAPP")
                ?: return KeymanInstallResult(false, "Keyman keyboard mode unavailable.")

            invokeStaticVoid(managerClass, "initialize", context, inAppKeyboardType)

            val keyboardFont = invokeField(managerClass, "KMDefault_KeyboardFont")?.toString().orEmpty()
                .ifBlank { "" }

            val keyboardInfoFallback = keyboardClass
                .getConstructor(
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    String::class.java,
                    java.lang.Boolean.TYPE,
                    String::class.java,
                    String::class.java
                )
                .newInstance(
                    DEFAULT_PACKAGE_ID,
                    DEFAULT_KEYBOARD_ID,
                    DEFAULT_KEYBOARD_NAME,
                    DEFAULT_LANGUAGE_ID,
                    DEFAULT_LANGUAGE_NAME,
                    DEFAULT_KEYBOARD_VERSION,
                    null,
                    "",
                    true,
                    keyboardFont,
                    keyboardFont
                )

            val keyboards = invokeStaticList(managerClass, "getKeyboardsList", context)
            val chosenKeyboard = selectInstalledKeyboard(keyboards, keyboardClass)
                ?: keyboards.firstOrNull()

            if (chosenKeyboard == null) {
                // Some Keyman versions only populate keyboard list after explicit add. Keep fallback.
                invokeStaticVoid(managerClass, "addKeyboard", context, keyboardInfoFallback)
            }

            val finalKeyboards = invokeStaticList(managerClass, "getKeyboardsList", context)
            val finalKeyboard = selectInstalledKeyboard(finalKeyboards, keyboardClass)
                ?: finalKeyboards.firstOrNull()
                ?: keyboardInfoFallback

            invokeStaticVoid(managerClass, "setDefaultKeyboard", finalKeyboard)
            invokeStaticVoid(managerClass, "setKeyboard", finalKeyboard)

            val keyboardId = keyboardClass.methods.firstOrNull { it.name == "getKeyboardID" }
                ?.invoke(finalKeyboard)
                ?.toString()
                .orEmpty()

            writeActiveAsset(context, chosenAsset)

            KeymanInstallResult(
                success = installedCount > 0 || finalKeyboards.isNotEmpty(),
                message = if (installedCount > 0 || finalKeyboards.isNotEmpty()) {
                    "Keyman package installed: $chosenAsset"
                } else {
                    "Keyman package processed: $chosenAsset"
                },
                installedKeyboardCount = finalKeyboards.size,
                currentKeyboardId = keyboardId,
                activePackageAsset = chosenAsset
            )
        }.getOrElse { error ->
            KeymanInstallResult(false, error.message ?: "Keyman install failed.")
        }
    }

    fun availablePackageFiles(context: Context): List<String> {
        return listKmpAssets(context)
    }

    fun activePackageFile(context: Context): String {
        val saved = prefs(context).getString("active_kmp_asset", "").orEmpty()
        return saved.ifBlank {
            selectPreferredKmpAsset(listKmpAssets(context)).orEmpty()
        }
    }

    private fun listKmpAssets(context: Context): List<String> {
        return runCatching {
            context.assets.list(KEYMAN_ASSET_DIR)
                ?.filter { it.endsWith(".kmp", ignoreCase = true) }
                ?.sorted()
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun selectPreferredKmpAsset(assets: List<String>): String? {
        if (assets.isEmpty()) return null

        // Prefer stable (avoid clearly marked pre-release builds).
        val stable = assets.filterNot { it.contains("pre", ignoreCase = true) || it.contains("beta", ignoreCase = true) }
        val candidates = if (stable.isNotEmpty()) stable else assets

        // Prefer higher semantic versions in filenames when present, otherwise fall back to lexical ordering.
        return candidates.maxWithOrNull { a, b ->
            val scoreA = assetScore(a)
            val scoreB = assetScore(b)
            when {
                scoreA != scoreB -> scoreA.compareTo(scoreB)
                else -> a.compareTo(b, ignoreCase = true)
            }
        }
    }

    private fun assetScore(name: String): Long {
        // Higher score wins.
        val semver = extractSemver(name)
        val (major, minor, patch) = semver ?: Triple(0, 0, 0)

        val semanticScore = (major.toLong() shl 40) + (minor.toLong() shl 20) + patch.toLong()
        val topicBonus = if (name.contains("amharic", ignoreCase = true) || name.contains("translit", ignoreCase = true)) 1L else 0L
        val stabilityBonus = if (name.contains("pre", ignoreCase = true) || name.contains("beta", ignoreCase = true)) 0L else 1L
        val defaultBonus = if (name.equals(DEFAULT_PACKAGE_FILE, ignoreCase = true)) 1L else 0L

        return semanticScore * 10L + topicBonus * 2L + stabilityBonus + defaultBonus
    }

    private fun extractSemver(name: String): Triple<Int, Int, Int>? {
        val match = Regex("""(?i)(?:^|[-_v])(\d+)\.(\d+)\.(\d+)(?:$|[-_.])""")
            .find(name)
            ?: return null
        return Triple(
            match.groupValues[1].toIntOrNull() ?: return null,
            match.groupValues[2].toIntOrNull() ?: return null,
            match.groupValues[3].toIntOrNull() ?: return null
        )
    }

    private fun selectInstalledKeyboard(keyboards: List<Any?>, keyboardClass: Class<*>): Any? {
        if (keyboards.isEmpty()) return null
        val getId = keyboardClass.methods.firstOrNull { it.name == "getKeyboardID" } ?: return keyboards.firstOrNull()
        val exact = keyboards.firstOrNull { keyboard ->
            getId.invoke(keyboard)?.toString().orEmpty().equals(DEFAULT_KEYBOARD_ID, ignoreCase = true)
        }
        return exact ?: keyboards.firstOrNull()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("keyman_installer", Context.MODE_PRIVATE)
    }

    private fun writeActiveAsset(context: Context, assetName: String) {
        runCatching {
            prefs(context).edit {
                putString("active_kmp_asset", assetName)
            }
        }
    }

    private fun copyAssetToCache(context: Context, assetName: String): File? {
        return runCatching {
            val cacheFile = File(context.cacheDir, assetName)
            context.assets.open("$KEYMAN_ASSET_DIR/$assetName").use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
            cacheFile
        }.getOrNull()
    }

    @Suppress("SameParameterValue")
    private fun invokeStringMethod(targetClass: Class<*>, methodName: String): String? {
        return runCatching {
            targetClass.methods.firstOrNull { it.name == methodName && it.parameterTypes.isEmpty() }
                ?.invoke(null)
                ?.toString()
        }.getOrNull()
    }

    @Suppress("SameParameterValue")
    private fun invokeField(targetClass: Class<*>, fieldName: String): Any? {
        return runCatching {
            targetClass.getField(fieldName).get(null)
        }.getOrNull()
    }

    @Suppress("SameParameterValue")
    private fun invokeStaticVoid(targetClass: Class<*>, methodName: String, vararg args: Any?): Boolean {
        return runCatching {
            val method = targetClass.methods.firstOrNull { candidate ->
                candidate.name == methodName && candidate.parameterTypes.size == args.size
            } ?: return false
            method.invoke(null, *args)
            true
        }.getOrDefault(false)
    }

    @Suppress("SameParameterValue")
    private fun invokeStaticList(targetClass: Class<*>, methodName: String, vararg args: Any?): List<Any?> {
        return runCatching {
            val method = targetClass.methods.firstOrNull { candidate ->
                candidate.name == methodName && candidate.parameterTypes.size == args.size
            } ?: return emptyList()

            when (val value = method.invoke(null, *args)) {
                is List<*> -> value.toList()
                is Array<*> -> value.toList()
                else -> emptyList()
            }
        }.getOrDefault(emptyList())
    }

    @Suppress("SameParameterValue")
    private fun enumValueOfClass(enumClass: Class<*>, name: String): Any? {
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            java.lang.Enum.valueOf(enumClass as Class<out Enum<*>>, name)
        }.getOrNull()
    }
}
