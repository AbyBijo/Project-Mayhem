# Security Policy

## Supported release

| Release | Security updates |
|---|---|
| Version 1 | Supported |

## Reporting a vulnerability

Do not open a public GitHub issue for a vulnerability that could expose document content, passwords, filesystem access, signing material, or arbitrary code execution.

Report it privately to:

- [abybijo1978@gmail.com](mailto:abybijo1978@gmail.com)
- [abybijo2025bca@mac.edu.in](mailto:abybijo2025bca@mac.edu.in)

Include:

- A clear description of the vulnerability
- Affected device and Android version
- Reproduction steps or a minimal proof of concept
- Expected impact
- Suggested remediation, if known

Remove real passwords and confidential documents. Use synthetic test data.

## Expected response

The developer will attempt to acknowledge a complete report within seven days, evaluate severity, and coordinate a fix before public disclosure. Response time may vary because this is an independently maintained project.

## Security design

Project Mayhem uses:

- Bundled offline application assets
- No Internet permission
- No broad external-storage permission
- Android Storage Access Framework folder grants
- Sanitised Markdown rendering
- Blocked external WebView navigation
- A restricted JavaScript-to-native PDF save bridge
- Release signing for APK integrity and updates

## Out of scope

- Vulnerabilities in a modified or unofficial APK
- Issues caused solely by a compromised Android device
- Social engineering without a software flaw
- Cloud-provider behaviour after the user deliberately selects that provider as the output folder
