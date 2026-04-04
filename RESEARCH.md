# Keyman and Amharic Keyboard Research Notes

This document is the focused research notebook for the **input side** of the project.

It covers:

- Keyman Engine for Android
- Amharic keyboard behavior
- transliteration patterns
- prediction / live suggestions
- the two reference repositories

It does **not** cover translation models. That work stays in `ROADMAP.md`.

## 1. Research Goal

The immediate question is:

> What is the best way to build Amharic typing and live suggestions for an Android app?

The options being studied are:

1. **Keyman-based input**
2. **Custom Android keyboard**
3. **Hybrid approach**

The research should answer which option is fastest, easiest to maintain, and most realistic for an open-source MVP.

## 2. Verified Facts About Keyman

Official Keyman docs say:

- Keyman Engine for Android 18.0 is a **Java library** for Android 5.0+.
- It supports a **fully customizable keyboard layout**.
- It can work **within an app** and **system-wide**.
- Keyman keyboard layouts are created with **Keyman Developer**.
- Keyman also has a **library of existing keyboard layouts**.
- Keyman uses **lexical models** for **predictive text** and **autocorrect**.

Sources:

- [Keyman Engine for Android](https://help.keyman.com/developer/engine/android/current-version/)
- [Developing lexical models](https://help.keyman.com/developer/current-version/guides/lexical-models/)

### Why this matters

Keyman splits the problem into two parts:

- **keyboard layout** for typing and character generation
- **lexical model** for prediction and autocorrect

That split is useful for this project because it prevents us from mixing transliteration logic and suggestion logic too early.

### Key classes documented by Keyman

The Android docs mention these classes:

- `KMManager`
- `KMKeyboard`
- `KeyboardEventHandler`

This suggests the Android integration has a clear engine layer and keyboard layer, which is useful if we want a clean architecture later.

## 3. What Keyman Means For This Project

### Likely strengths

- supports Android directly
- supports in-app and system-wide input
- allows keyboard customization
- gives us a path to predictive text through lexical models
- lowers the amount of keyboard logic we need to invent ourselves

### Likely tradeoffs

- Keyman may solve typing better than it solves app-specific UX
- prediction will likely need separate data work
- if we need very custom behavior, the engine may still need extra glue code

### Research inference

Because Keyman documents keyboards and lexical models separately, the safest working assumption is:

- **keyboard/transliteration** and **prediction/suggestions** should be researched as separate layers

That is an inference from the docs, not a direct promise from the API.

## 4. Reference Repository 1: Android Custom Keyboard

Repository:

- [android_amharic_custom_keyboard](https://github.com/EthioCompSciClub/android_amharic_custom_keyboard)

What the repo description says:

- it shows how to create a **custom Android Amharic soft keyboard** in Android Studio
- it starts from the usual custom keyboard pattern and adapts it for Amharic
- it recognizes that Amharic has **more than 26 characters**

### What to study from it

- Android keyboard structure
- input method flow
- how key presses become Amharic characters
- how the project handles Amharic character complexity
- whether the implementation is simple enough to maintain

### What to learn specifically

- how it handles special characters
- how it maps multiple Latin inputs to one Amharic output
- whether the keyboard is hardcoded or table-driven
- whether the code is reusable or tightly coupled to the demo app

### Research note

This repository is useful as an **Android implementation reference**, but it may not be the final architecture.

It is especially useful if Keyman turns out to be too limiting for the exact UX we want.

## 5. Reference Repository 2: Amharic-Keyboard

Repository:

- [Amharic-Keyboard](https://github.com/dawityise/Amharic-Keyboard)

What the repo says:

- it is a **jQuery plugin for Amharic keyboard support online**
- it can be attached to multiple text areas
- it includes a typing map for Amharic output

### Important note

This is **not an Android keyboard**.

It is still very useful because it exposes transliteration behavior and character mapping rules.

### Useful examples from the typing map

The repository documents mappings such as:

| Typed | Output |
| --- | --- |
| `he` | `ሀ` |
| `hu` | `ሁ` |
| `hi` | `ሂ` |
| `ha` | `ሃ` |
| `hee` | `ሄ` |
| `h` | `ህ` |
| `ho` | `ሆ` |
| `hua` | `ኋ` |
| `ae` | `አ` |
| `au` | `ኡ` |
| `ai` | `ኢ` |
| `aa` | `ኣ` |
| `aee` | `ኤ` |
| `a` | `እ` |
| `ao` | `ኦ` |
| `be` | `በ` |
| `bu` | `ቡ` |
| `bi` | `ቢ` |
| `ba` | `ባ` |
| `bee` | `ቤ` |
| `b` | `ብ` |
| `bo` | `ቦ` |
| `bua` | `ቧ` |
| `hue` | `ኃ` |
| `hui` | `ኅ` |

Source:

- [Amharic-Keyboard README](https://github.com/dawityise/Amharic-Keyboard)

### What to learn from it

- how Latin sequences map to Amharic syllables
- how long and short sequences behave
- how special forms are represented
- what typing conventions feel intuitive

### License note

The repo is MIT licensed, which is useful for learning patterns, but we should still verify reuse rules before copying any code directly.

## 6. Transliteration Notes

The main transliteration problem is not just “replace Latin letters with Amharic letters.”

It is more like:

- detect the full typed sequence
- decide whether the next character changes the output
- support special cases and exceptions
- avoid accidental ambiguity

### Examples of transliteration behavior

- Some outputs are very short and predictable, like `he` -> `ሀ`
- Some outputs require special handling, like `hue` -> `ኃ`
- Some sequences are single characters that still need context, like `a` -> `እ`

### Questions that still need research

- What happens when a sequence could map to more than one result?
- Should the keyboard prefer the shortest match or the most common match?
- Should the keyboard show candidates, or always commit one result?
- How should backspace behave after a multi-character transliteration?

## 7. Prediction / Live Suggestions Notes

Keyman’s lexical model docs make one thing clear:

- prediction and autocorrect are powered by a **lexical model**

### Working assumption

For this project, live suggestions should probably not be treated as a keyboard-only feature.

Instead, the suggestion system likely needs:

- a word list
- frequency data
- a ranking strategy
- a way to update or extend the vocabulary later

### What we should test

- whether a small word list gives useful suggestions
- whether suggestions appear fast enough on Android
- whether prediction improves typing or adds confusion
- whether a simple rules-based approach is enough for the first release

### Early MVP idea

If prediction data is weak, the first version can still be useful with:

- transliteration
- basic suggestions
- a limited vocabulary

That is better than waiting for a perfect model.

## 8. Architecture Options To Compare

### Option A: Keyman-first

Use Keyman for the keyboard and lexical model layers.

Strengths:

- faster start
- official Android support
- clear separation of keyboard and prediction

Risks:

- some desired behavior may require extra glue
- we depend on Keyman’s model of the world

### Option B: Custom Android keyboard

Build an Android IME from scratch.

Strengths:

- maximum control
- can be tuned exactly to the app

Risks:

- more work
- more edge cases
- harder to keep clean as an open-source project

### Option C: Hybrid

Use Keyman as the main keyboard research path, but keep room for custom Android code if needed.

Strengths:

- practical and flexible
- lets us start with a proven engine
- preserves an escape hatch if the UX needs custom behavior

Risks:

- the architecture can become unclear unless boundaries are written down early

## 9. Decision Criteria

When comparing the three options, use these criteria:

### Speed

- Which option gets a usable keyboard working fastest?

### Maintainability

- Which option will a future contributor understand most easily?

### Offline support

- Which option is easiest to keep working without internet?

### Prediction quality

- Which option lets us add useful suggestions without too much complexity?

### Reuse

- Which option lets us reuse existing mappings or code?

## 10. Known Risks

### Risk: transliteration becomes too clever

If the mapping logic becomes overly complex, the keyboard will be hard to debug.

Mitigation:

- keep mappings table-driven where possible
- keep special cases visible in the data

### Risk: prediction has no data

If we do not have a decent word list, suggestions may be noisy or useless.

Mitigation:

- start with a small, curated word list
- test on real typing examples early

### Risk: Android keyboard work is larger than expected

IME work can get complicated fast.

Mitigation:

- prototype first
- keep the first version narrow
- avoid mixing translation logic into the keyboard layer

### Risk: copying patterns without understanding them

The reference projects are useful, but they do not automatically fit our exact goals.

Mitigation:

- treat them as references, not final architecture
- record what is reusable and what is not

## 11. Research Checklist

### Keyman research tasks

- confirm Android version support
- confirm in-app keyboard flow
- confirm system-wide keyboard flow
- study how to build keyboard layouts
- study how lexical models are created
- study how prediction is connected to the keyboard
- inspect how Keyman apps are packaged and distributed

### Android keyboard research tasks

- study the custom keyboard repository structure
- understand the Android services involved in input methods
- identify key event handling
- identify text insertion / commit behavior
- compare maintainability against Keyman

### Transliteration research tasks

- collect the common Amharic syllable families
- map the most common Latin input sequences
- find ambiguities and special cases
- decide whether the keyboard should show candidates or auto-commit

### Prediction research tasks

- build a small word list
- decide ranking rules
- test suggestion speed
- test how predictions behave with transliteration

## 12. Tentative Conclusion

The current best guess is:

- **Keyman** is a strong candidate for the keyboard and suggestion layer
- **android_amharic_custom_keyboard** is a useful Android implementation reference
- **Amharic-Keyboard** is a useful transliteration pattern reference
- the safest first design is to keep **typing** and **prediction** as separate workstreams

That is still a research conclusion, not a final implementation decision.

## 13. What This File Is For

Use this file when you want to answer questions like:

- What should we study first?
- What does Keyman actually support?
- What can we learn from the two existing keyboard projects?
- Which keyboard path seems best for the first open-source version?

Use `ROADMAP.md` when you want the larger project plan.

