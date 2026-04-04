# Amharic Offline Translator - Android

This folder contains the native Kotlin Android version of the app.

## What it includes

- Jetpack Compose UI
- offline English to Amharic phrasebook translation
- transliteration fallback for unknown text
- live Amharic typing preview
- example chips and suggestion chips
- a dark, warm UI that matches the research goal

## Open In Android Studio

Open this `android/` folder as a project in Android Studio.

The project files are already structured for a single `:app` module.
If Android Studio asks to generate or refresh Gradle wrapper files, let it do so in this folder.

## Notes

- The app is designed to work offline.
- The translation engine is local and deterministic.
- The next upgrade path is to replace the keyboard/input layer with Keyman-based integration when ready.
