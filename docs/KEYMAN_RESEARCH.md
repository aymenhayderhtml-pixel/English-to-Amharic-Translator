# Keyman Research Notes

This document collects the practical research questions for the input layer of the project.

## What we know

- Keyman Engine for Android supports Android 5.0+.
- It can be used for in-app keyboards and system-wide keyboards.
- Keyboard layouts are authored with Keyman Developer.
- Predictive text and autocorrect are handled through lexical models.

## What we still need to verify

- Can Keyman support the exact Amharic transliteration behavior we want?
- Is the prediction experience good enough for live suggestions?
- Is the Android integration lightweight enough for a first release?
- Do we need a Keyman keyboard only, or a hybrid with custom Android code?

## Reusable references

- [android_amharic_custom_keyboard](https://github.com/EthioCompSciClub/android_amharic_custom_keyboard)
- [Amharic-Keyboard](https://github.com/dawityise/Amharic-Keyboard)

## Early conclusion

The best working assumption is to keep transliteration and prediction separate until we prove the keyboard behavior on Android.

