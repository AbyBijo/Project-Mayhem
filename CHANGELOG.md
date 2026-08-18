# Changelog

All notable changes to Project Mayhem are documented here.

## Unreleased

### Fixed

- Android CI after the AndroidX Activity 1.13.0 and Android Gradle Plugin 9.3.1 upgrades by compiling and targeting API 36
- Broken README link to the deleted GitHub launch walkthrough

## Version 1 — 2026-08-09

### Added

- Offline-first Android PDF editor
- Live A4 page preview
- Markdown source editor and formatting toolbar
- Sanitised rendered Markdown preview
- Configurable title font, alignment, size, weight, spacing, and margin
- Optional metadata line with independent alignment and size
- Single-column and double-column body layouts
- Repeating footer and automatic page numbering
- Optional colour image, caption, and first-page or last-page placement
- Configurable diagonal watermark
- Standard and negative PDF output
- Optional PDF password protection
- One-time Android output-folder setup
- Persisted Storage Access Framework permission
- Direct PDF writing to the selected folder
- Offline fonts and PDF-rendering dependencies
- Adaptive, round, legacy, and monochrome app icons
- GitHub README, illustrated PDF guide, CI workflow, and community templates

### Security

- No Internet permission
- No broad storage permission
- Controlled local WebView asset origin
- External WebView navigation blocking
- Restricted JavaScript bridge
- PDF signature and release-build verification process
