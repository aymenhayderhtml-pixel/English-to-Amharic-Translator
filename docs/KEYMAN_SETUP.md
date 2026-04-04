# Keyman Setup

## What Keyman Gives Us

- In-app keyboards
- System-wide keyboards
- Lexical models for prediction and autocorrect

Keyman Engine for Android is documented as a Java library for Android that supports both in-app and system-wide keyboards.

## What We Need To Add

The Keyman Android SDK is distributed as a downloadable archive. The archive includes:

- the Android `.aar` library
- sample projects

To wire Keyman into this app, add the official SDK package contents locally and then connect the keyboard package and lexical model package.

## Recommended Setup Steps

1. Download the official **Keyman for Android SDK** archive from the Keyman developer site.
2. Extract the archive to a working folder on your machine.
3. Find the Android `.aar` file included in the archive. In this project it is expected at:
   - `android/app/libs/keyman-engine.aar`
4. Copy your compiled keyboard package `.kmp` into:
   - `android/app/src/main/assets/keyman/`
5. Copy your lexical model package into the same folder.
6. Add the SDK library to the Android Studio project using the sample project structure from the Keyman SDK archive.
7. Register the keyboard package with `KMManager`.
8. Register the lexical model with `KMManager`.
9. Keep the current offline translator as a fallback until the keyboard flow is stable.

## Current Repo Status

The app already has:

- a mission-control screen
- a Keyman bridge card
- a local translator
- a local learning/autocorrect engine

That means the remaining work is the official Keyman SDK drop-in, not a redesign of the app.

## Current Android Module Wiring

The app module is already configured to load:

- `android/app/libs/keyman-engine.aar`

That means Android Studio should now recognize the SDK once you sync the project.

## Sources

- [Keyman Engine for Android](https://help.keyman.com/developer/engine/android/)
- [Guide: build an in-app keyboard for Android with Keyman Engine](https://help.keyman.com/developer/engine/android/current-version/guides/in-app/)
- [Guide: build a system keyboard app for Android with Keyman Engine](https://help.keyman.com/developer/engine/android/current-version/guides/system-keyboard/)
- [Keyman lexical models](https://help.keyman.com/developer/current-version/guides/lexical-models/)
