package dev.amharictranslator.keyman

import android.content.Context

data class KeymanPackageSpec(
    val packageId: String,
    val displayName: String,
    val description: String
)

data class KeymanBridgeState(
    val bridgeReady: Boolean,
    val inAppTarget: String,
    val systemTarget: String,
    val installResult: KeymanInstallResult,
    val keyboardPack: KeymanPackageSpec,
    val lexicalModelPack: KeymanPackageSpec,
    val installedAssets: List<String>,
    val availableKeyboardPackages: List<String>,
    val activeKeyboardPackage: String,
    val setupChecklist: List<String>
)

object KeymanBridge {
    fun state(context: Context): KeymanBridgeState {
        val installedAssets = listAssetCandidates(context)
        val availableKeyboardPackages = KeymanPackageInstaller.availablePackageFiles(context)
        val activeKeyboardPackage = KeymanPackageInstaller.activePackageFile(context)

        return KeymanBridgeState(
            bridgeReady = true,
            inAppTarget = "Embed the keyboard inside the Translator screen first.",
            systemTarget = "Promote the same keyboard to a system IME later.",
            installResult = KeymanInstallResult(
                success = false,
                message = if (activeKeyboardPackage.isBlank()) {
                    "Tap Load Keyman package to check the keyboard installation."
                } else {
                    "Tap Load Keyman package. Last package: $activeKeyboardPackage"
                },
                activePackageAsset = activeKeyboardPackage
            ),
            keyboardPack = KeymanPackageSpec(
                packageId = "amharic.transliteration",
                displayName = "Amharic Transliteration Keyboard",
                description = "English typing maps to Amharic script through a Keyman keyboard package."
            ),
            lexicalModelPack = KeymanPackageSpec(
                packageId = "amharic.lexical.model",
                displayName = "Amharic Prediction Dictionary",
                description = "A word-list lexical model powers prediction and autocorrect."
            ),
            installedAssets = installedAssets,
            availableKeyboardPackages = availableKeyboardPackages,
            activeKeyboardPackage = activeKeyboardPackage,
            setupChecklist = listOf(
                "Add the Keyman Android SDK AAR to the app module.",
                "Place compiled .kmp keyboard packages in app/src/main/assets/keyman/.",
                "Use Reload keyboard after swapping a .kmp package in assets.",
                "Place the lexical model package in app/src/main/assets/keyman/.",
                "Register the lexical model and associate it with the Amharic keyboard.",
                "Keep the current translator as the fallback while the bridge is being proven."
            )
        )
    }

    fun missionControlSummary(): List<String> {
        return listOf(
            "Mission control manages install, setup, model status, and training memory.",
            "The keyboard layer handles English-to-Amharic typing.",
            "The lexical model handles prediction and autocorrect.",
            "The translator remains a separate offline sentence engine."
        )
    }

    private fun listAssetCandidates(context: Context): List<String> {
        return runCatching {
            context.assets.list("keyman")
                ?.filter {
                    it.endsWith(".kmp", ignoreCase = true) ||
                        it.endsWith(".kmn", ignoreCase = true) ||
                        it.endsWith(".json", ignoreCase = true)
                }
                ?.toList()
                .orEmpty()
        }.getOrDefault(emptyList())
    }
}
