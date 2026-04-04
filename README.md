# Amharic-English Offline Translator

An Android-first open-source project for:

- offline English to Amharic translation
- Amharic typing support
- live input suggestions / prediction

This repository now has:

- a browser-based offline prototype at the root
- a Kotlin Android app under `android/`

## Runnable Prototype

A first offline prototype now lives in the root of this repo as a static app:

- `index.html`
- `styles.css`
- `app.js`
- `manifest.json`
- `sw.js`

It includes:

- English to Amharic phrase translation
- transliteration fallback for unknown text
- a live Amharic typing preview
- example chips and suggestion chips
- offline caching through a service worker

### Run it locally

Use any static server. For example:

```powershell
python -m http.server 8000
```

Then open `http://localhost:8000` in your browser.

If you open `index.html` directly, the UI still works, but the service worker will not register.

## Kotlin Android App

The native Kotlin version lives in [`android/`](android/).

It includes:

- Jetpack Compose UI
- the same local phrasebook translation logic
- a live Amharic typing preview
- example chips and suggestion chips
- offline-first design

Open the `android/` folder in Android Studio to work on the native app.

## Current Direction

The project is being researched as two connected parts:

1. **Translation**
   - English text in
   - Amharic text out
   - offline-capable

2. **Input**
   - Amharic keyboard support
   - transliteration from English typing to Amharic characters
   - live suggestions / prediction

## Why Keyman Is In Scope

Keyman is a strong candidate for the input side because its Android engine supports:

- in-app keyboards
- system-wide keyboards
- custom keyboard layouts
- lexical models for prediction and autocorrect

Useful references:

- [Keyman Engine for Android](https://help.keyman.com/developer/engine/android/current-version/)
- [Developing lexical models](https://help.keyman.com/developer/current-version/guides/lexical-models/)

## Reference Projects

These repositories are practical study cases for the input layer:

- [android_amharic_custom_keyboard](https://github.com/EthioCompSciClub/android_amharic_custom_keyboard)
- [Amharic-Keyboard](https://github.com/dawityise/Amharic-Keyboard)

### What to learn from them

- how Amharic characters are mapped from typed Latin input
- how a keyboard UI is structured in Android or web contexts
- what transliteration patterns are reusable
- what parts could be improved with prediction or better UX

## Research Findings So Far

### Keyman

Keyman Engine for Android is a Java library for Android 5.0+ that supports fully customizable keyboards within an app and system-wide. Keyman keyboards can be built with Keyman Developer, and prediction/autocorrect comes from a separate lexical model.

This matters because:

- keyboard input and prediction are related, but they are not the same thing
- a lexical model is needed for suggestions/autocorrect
- a keyboard layout can handle transliteration while the lexical model handles prediction

### Amharic keyboard examples

The `Amharic-Keyboard` project documents Latin-to-Amharic typing pairs such as:

- `he` -> `ሀ`
- `hu` -> `ሁ`
- `hi` -> `ሂ`
- `ha` -> `ሃ`
- `hee` -> `ሄ`
- `h` -> `ህ`
- `ho` -> `ሆ`

This is useful as a starting point for transliteration rules.

The `android_amharic_custom_keyboard` project is an Android example showing how a custom Amharic soft keyboard can be built in Android Studio.

## Recommended MVP Direction

### First release

- Android-first
- English input to Amharic output
- offline-capable
- live suggestions for Amharic typing
- small, practical scope

### Recommended architecture

- **Input layer:** Keyman-based keyboard research first
- **Prediction layer:** Keyman lexical model research
- **Translation layer:** existing offline model or library first, then improve later

### Why this is the safest path

- avoids training everything from scratch
- gives a usable open-source MVP sooner
- keeps keyboard work separate from translation work
- leaves room for later quality improvements

## Roadmap

### Phase 1: Research

- compare Keyman with custom Android keyboard approaches
- review transliteration patterns from the reference repos
- identify offline translation model options
- collect example English-Amharic sentence pairs for evaluation

### Phase 2: MVP design

- choose the keyboard strategy
- decide whether translation runs fully on-device or via packaged local models
- define the first screen flow
- define prediction behavior and offline behavior

### Phase 3: Build

- implement the input method
- implement translation UI and core translation flow
- add prediction/autocorrect if data is ready
- test on a real Android device

### Phase 4: Open-source release

- add license
- add contribution guide
- document setup and limitations
- publish the first release once the MVP is stable

## Open Questions

- Should Keyman be the main keyboard layer, or only a reference?
- Should prediction come from a Keyman lexical model or from custom logic?
- Which offline translation engine gives the best balance of quality and size?
- Should the first release support only English to Amharic, or also Amharic to English later?

## Next Steps

1. Research Keyman Android integration details.
2. Study the two Amharic keyboard references in more depth.
3. Compare offline translation engines for Amharic-English support.
4. Convert the research results into a build checklist for the MVP.
