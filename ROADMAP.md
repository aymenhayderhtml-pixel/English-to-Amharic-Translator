# Amharic-English Offline Translator + Amharic Keyboard Roadmap

This file is the detailed research and build roadmap for the project. The goal is to keep the work open-source, Android-first, and practical for a first release.

The current focus is **research and architecture**, not implementation.

## 1. Project Goal

Build an Android app that helps users:

- type Amharic more easily
- see live input suggestions while typing
- translate English to Amharic offline
- use the app without depending on internet access

The project is being treated as two connected systems:

1. **Input system**
   - Amharic typing
   - transliteration from Latin typing to Amharic characters
   - live suggestions / prediction

2. **Translation system**
   - English text in
   - Amharic text out
   - offline translation path

## 2. Scope for the First Release

### In scope

- Android-first app
- English to Amharic translation
- Amharic keyboard support
- live typing suggestions
- offline support
- open-source release

### Out of scope for now

- training a translation model from scratch
- iOS support
- desktop support
- cloud-only translation
- full multilingual support
- perfect translation quality on day one

## 3. Why Keyman Is Part of the Research

Keyman is a strong candidate for the input side of the project.

According to the official docs, **Keyman Engine for Android** is a Java library for Android 5.0+ that supports a fully customizable keyboard layout both inside an app and system-wide. Keyboards can be created with Keyman Developer.

Official references:

- [Keyman Engine for Android](https://help.keyman.com/developer/engine/android/current-version/)
- [Keyman Developer guides](https://help.keyman.com/developer/current-version/guides/)
- [Lexical models for predictive text and autocorrect](https://help.keyman.com/developer/current-version/guides/lexical-models/)

### Why it matters

- a keyboard layout handles typing behavior and transliteration
- a lexical model handles prediction and autocorrect
- the app can research these separately instead of mixing them too early

## 4. Reference Projects To Study

These repositories are not the final target, but they are useful design references.

### 4.1 Android custom keyboard example

- [android_amharic_custom_keyboard](https://github.com/EthioCompSciClub/android_amharic_custom_keyboard)

What to study there:

- Android keyboard structure
- input method flow
- how the keyboard maps keys to Amharic characters
- how the app handles the complexity of Amharic characters
- whether the design is easy to maintain or too tightly coupled

Important note:

- this repo is a direct Android example
- it is useful for architecture and keyboard behavior
- it is not automatically the best long-term solution

### 4.2 Amharic typing pattern reference

- [Amharic-Keyboard](https://github.com/dawityise/Amharic-Keyboard)

What to study there:

- Latin-to-Amharic transliteration patterns
- character combinations
- how one typed sequence maps to one Amharic output
- special character handling
- the typing rules that can be reused in an Android implementation

Important note:

- this project is a jQuery plugin for online keyboard support
- it is useful as a mapping reference
- it is not an Android keyboard by itself

### 4.3 Concrete examples from the typing map

The repo includes patterns such as:

- `he` -> `ሀ`
- `hu` -> `ሁ`
- `hi` -> `ሂ`
- `ha` -> `ሃ`
- `hee` -> `ሄ`
- `h` -> `ህ`
- `ho` -> `ሆ`

These patterns are useful as a baseline transliteration family to compare against Keyman or a custom Android keyboard.

## 5. Research Questions

These are the decisions the research should answer before implementation starts.

### Keyboard and input

- Can Keyman handle the Amharic input experience we want?
- Does Keyman give us a faster path than building a custom Android keyboard from scratch?
- Is the keyboard better built as:
  - a Keyman keyboard
  - a custom Android IME
  - a hybrid of both
- Can the typing experience support both simple transliteration and special Amharic characters cleanly?

### Prediction and suggestions

- Can Keyman lexical models support the live suggestions we want?
- Do we have enough word data to make prediction useful?
- Should suggestions be based on:
  - keyboard history
  - a word list
  - language frequency data
  - custom logic

### Translation

- Which offline translation approach is realistic for the first release?
- What is the smallest useful translation stack we can ship on Android?
- Should translation be:
  - fully on-device
  - packaged with downloaded local assets
  - hybrid, with offline-first behavior

### Product direction

- What should the first public MVP include?
- What can wait until a later version?
- How much should the app try to do at once:
  - keyboard only
  - translation only
  - both together

## 6. Decision Criteria

Use the following criteria when comparing options.

### Speed to MVP

- how quickly the app can become usable
- how much custom code is required
- how much research is needed before the first demo

### Offline reliability

- does the feature still work with no internet?
- does it depend on remote APIs?
- does it need a large model download?

### Maintainability

- can one person understand and maintain the code?
- is the architecture simple enough for open-source contributors?
- are the input rules isolated from the translation logic?

### Quality

- are the predictions useful?
- are the translations understandable?
- are the keyboard mappings natural for Amharic users?

### Portability

- is the solution Android-friendly?
- can the design survive future upgrades?
- does it depend on brittle platform-specific hacks?

## 7. Roadmap Phases

## Phase 1: Research Sprint

### Goal

Collect enough information to decide the keyboard and translation architecture.

### Tasks

- read the Keyman Android documentation
- read the Keyman lexical model documentation
- inspect the two reference repositories
- list reusable transliteration patterns
- identify Amharic typing edge cases
- list candidate offline translation approaches
- note licensing and reuse constraints for all references

### Deliverables

- a short research memo
- a feature comparison between Keyman and custom keyboard approaches
- a transliteration sample table
- a list of translation engine candidates
- a recommendation for the MVP architecture

### Exit criteria

- the team can explain the recommended input strategy
- the team can explain the recommended prediction strategy
- the team can explain the recommended translation strategy

## Phase 2: Architecture Decision

### Goal

Turn the research into a clear implementation plan.

### Tasks

- define the app modules
- define how input and translation communicate
- choose whether the keyboard is embedded, system-wide, or both
- choose whether prediction lives in Keyman or in custom logic
- define what data needs to ship with the app
- define what can be downloaded later

### Deliverables

- architecture notes
- module boundaries
- data flow diagram
- MVP feature list
- non-goals list

### Exit criteria

- no major product decision is still unresolved
- the next implementation step can be started without guessing

## Phase 3: Proof of Concept

### Goal

Validate the hardest technical risks before building the full app.

### Tasks

- prototype the Amharic input path
- test Keyman in an Android context if selected
- test predictive suggestions with a small word list if selected
- test one offline translation pipeline
- check how quickly the app loads and responds

### Deliverables

- a small working prototype
- notes on latency, memory, and UX
- a list of blockers and follow-up work

### Exit criteria

- the chosen architecture works on a real Android device
- the app remains usable offline

## Phase 4: MVP Build

### Goal

Build the first public version of the app.

### Must-have features

- English input field
- Amharic output area
- offline translation path
- Amharic typing support
- live suggestions or prediction
- copy/share actions
- basic history or recent translations

### Nice-to-have features

- favorites
- phrase suggestions
- dark mode
- translation history search
- text-to-speech

### Deliverables

- a usable Android MVP
- a simple settings screen
- a release-ready README

### Exit criteria

- a new user can install the app and translate offline
- the app does not depend on hidden manual setup

## Phase 5: Open-Source Release Prep

### Goal

Make the repository ready for public contribution.

### Tasks

- choose an open-source license
- add contribution guidelines
- document setup steps
- document known limitations
- add issue templates
- add a changelog or release notes
- clean up folder structure and naming

### Deliverables

- public-ready repository
- contributor-friendly documentation
- first tagged release

## 8. Detailed Research Checklist

## A. Keyman research

- confirm Android support requirements
- confirm in-app keyboard support
- confirm system-wide keyboard support
- study how keyboard layouts are authored
- study how lexical models are created
- study how prediction connects to the keyboard
- review testing and distribution steps

## B. Android keyboard research

- study how a custom Android IME is built
- identify the core Android services and lifecycle pieces
- inspect key handling and text commitment logic
- compare keyboard UI complexity against Keyman
- identify what would be hard to maintain long term

## C. Transliteration research

- list Amharic consonant and vowel families
- map the most common Latin input sequences
- identify ambiguous sequences
- handle special characters and exceptions
- decide how suggestions should behave when multiple outputs are possible

## D. Prediction research

- collect a small word list
- identify word frequency sources
- define suggestion ranking logic
- decide whether prediction should be simple or model-based
- test whether the suggestion bar improves typing speed

## E. Translation research

- identify a usable offline translation path
- evaluate model size versus quality
- test inference speed on Android hardware
- define what happens when a translation is uncertain
- decide whether to support fallback behavior

## 9. Quality Metrics

These are the practical signals that the project is moving in the right direction.

### Input metrics

- typing feels natural
- common sequences produce the expected Amharic output
- special characters do not break the keyboard
- suggestions appear quickly enough to be useful

### Translation metrics

- translations are understandable
- the app works without network access
- the model fits mobile constraints
- response time is acceptable on a mid-range phone

### Product metrics

- a new contributor can understand the repo
- the README explains the project clearly
- the MVP can be installed and tested without developer help

## 10. Risks And Mitigations

### Risk: keyboard complexity grows too fast

Mitigation:

- keep the transliteration rules separate from the UI
- avoid clever logic until the basic flow works

### Risk: prediction is weak without good data

Mitigation:

- start with a small word list
- test suggestions early
- improve the data iteratively

### Risk: offline translation is too heavy for Android

Mitigation:

- test the smallest viable model first
- measure memory and startup cost early
- keep a fallback plan if the model is too large

### Risk: scope becomes too large

Mitigation:

- keep the first release focused on English to Amharic
- treat reverse translation as later work
- treat extra features as optional

### Risk: the project becomes hard for contributors to join

Mitigation:

- document architecture early
- keep modules small
- write clear issue labels and contribution notes

## 11. Recommended Repository Structure For Later

This is not implementation yet, but it is the shape the final project should aim for.

- `README.md` for the short public intro
- `ROADMAP.md` for the long-form plan
- `docs/` for research notes and design decisions
- `assets/` for sample data and screenshots
- `app/` for the Android application code
- `models/` or `data/` for local prediction or translation resources if needed

## 12. Definition Of Done For The Research Stage

The research stage is complete when the project has:

- a clear recommendation for the keyboard/input layer
- a clear recommendation for prediction
- a clear recommendation for offline translation
- a list of reference patterns from the two sample repos
- a small set of test sentences or sample inputs
- a roadmap that the implementation stage can follow without guessing

## 13. Immediate Next Actions

1. Read the Keyman Android docs carefully.
2. Compare Keyman against a custom Android keyboard path.
3. Extract transliteration patterns from the two reference repos.
4. Decide the first MVP boundary.
5. Turn the research notes into implementation tickets.

## 14. Working Assumption

The safest current assumption is:

- Android first
- offline first
- Keyman as the main input research path
- separate prediction research through lexical models
- translation handled by a mobile-friendly offline engine

