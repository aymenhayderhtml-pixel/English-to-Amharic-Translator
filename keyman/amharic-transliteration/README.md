# Amharic Transliteration Keyboard

This folder holds the first Keyman keyboard source for the project.

Goal:

- type English letters
- convert them into Amharic script
- keep the keyboard package ready for prediction/autocorrect later

Source files:

- `HISTORY.md`
- `source/amharic-transliteration.kps`
- `source/amharic-transliteration.kmn`
- `source/readme.htm`
- `source/welcome.htm`

Build output:

- `build/` for generated release artifacts

Build note:

- compile this source with Keyman Developer to produce the distributable `.kmp`
- then copy the compiled package into `android/app/src/main/assets/keyman/`
- the `.kps` follows the Keyman package template format used by Keyman Developer

This first version is intentionally small and focused on the transliteration behavior used by the Android app.
