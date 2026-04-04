# Contributing to Amharic-English Offline Translator

Thanks for helping improve the project.

This repository is currently focused on:

- Android-first offline translation
- Amharic keyboard/input research
- live suggestions and prediction
- open-source documentation and roadmap work

## Before You Start

- Read [README.md](README.md)
- Read [ROADMAP.md](ROADMAP.md)
- Read [RESEARCH.md](RESEARCH.md)
- Check open issues before starting a new task

## Good First Contributions

- improve the README or roadmap
- add more English-to-Amharic example phrases
- refine transliteration rules
- test the Android project structure
- improve UI polish in the Kotlin app
- research and document Keyman integration options

## Development Notes

The repo currently contains:

- a Kotlin Android app in [`android/`](android/)
- a browser prototype at the root for quick testing

When contributing:

- keep changes small and focused
- prefer offline-first behavior
- avoid introducing network dependencies unless the roadmap calls for it
- keep keyboard logic separate from translation logic when possible

## Suggested Workflow

1. Fork or create a branch.
2. Make one focused change.
3. Test the change if possible.
4. Open a pull request with a clear description.

## Commit Message Style

Use short, descriptive commit messages such as:

- `Add more phrasebook entries`
- `Improve Amharic typing preview`
- `Document Keyman research`
- `Polish Android startup screen`

## Code Style

- Use ASCII by default unless Amharic text is needed.
- Keep Kotlin code readable and modular.
- Keep documentation simple and clear.
- Prefer comments only when the code is not obvious.

## Issues

If you open an issue, include:

- what you expected
- what happened instead
- steps to reproduce
- screenshots if relevant
- the platform or file involved

## License

By contributing, you agree that your changes will be licensed under the MIT License in this repository.
