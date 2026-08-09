# Contributing to Project Mayhem

Thank you for helping improve Project Mayhem.

## Before opening an issue

- Search existing issues.
- Read the [user guide](docs/Project-Mayhem-User-Guide.pdf).
- Remove passwords, private documents, email content, and other confidential data from screenshots.
- For security vulnerabilities, follow [SECURITY.md](SECURITY.md) instead of opening a public issue.

## Development setup

Requirements:

- JDK 17
- Android SDK Platform 35
- Git

Build and validate:

```bash
./gradlew lintDebug assembleDebug
```

The APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Pull request workflow

1. Fork the repository.
2. Create a focused branch from `main`.
3. Make one logically related change.
4. Test on a supported Android device or emulator.
5. Run lint and the debug build.
6. Update documentation and screenshots when behaviour changes.
7. Open a pull request using the template.

Suggested branch names:

```text
feature/document-presets
fix/folder-grant-recovery
docs/export-guide
security/webview-policy
```

## Commit messages

Use short, action-oriented conventional prefixes where practical:

```text
feat: add document preset support
fix: restore output folder state
docs: explain encrypted exports
security: reject unexpected navigation
build: update AndroidX dependencies
```

## Project principles

Changes should preserve these qualities:

- Offline-first document creation
- No unnecessary accounts or network dependency
- Least-privilege Android storage access
- Predictable PDF output
- Clear, focused mobile interaction
- No secrets or personal document content in the repository

## Code review checklist

A reviewer should verify:

- The change solves the stated problem.
- One-time output-folder setup still works.
- PDFs are written to the chosen document tree.
- Markdown remains sanitised.
- External WebView navigation remains blocked.
- No broad storage or Internet permission was added without a documented reason.
- User-visible changes are documented.

## Signing keys

Never commit or share:

```text
*.jks
*.keystore
keystore.properties
local.properties
```

Release APKs must be signed by the project owner using the original private key.
