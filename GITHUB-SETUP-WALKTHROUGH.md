# Project Mayhem — GitHub Launch Walkthrough

This checklist turns `Project-Mayhem` into a polished public repository at:

**https://github.com/AbyBijo/Project-Mayhem**

---

## 1. Create the repository

1. Sign in to [GitHub](https://github.com).
2. Click **New repository**.
3. Set the owner to **AbyBijo**.
4. Enter the repository name exactly as:

   ```text
   Project-Mayhem
   ```

5. Paste this in the **Description** field:

   > Offline-first Android Markdown editor for creating stylized, borderless A4 PDFs with live preview, images, watermarks, password protection, negative mode, and one-time folder setup—no internet or broad storage permission.

6. Choose **Public** if you want people to discover and download it.
7. Do **not** initialise it with a README, `.gitignore`, or license—the project already contains a README and `.gitignore`.
8. Click **Create repository**.

### Recommended About settings

After the repository exists, click the gear icon beside **About** and use:

- **Description:** the sentence above
- **Website:** `https://github.com/AbyBijo/Project-Mayhem/releases/latest`
- **Topics:**

  ```text
  android
  pdf-generator
  pdf-editor
  markdown-editor
  offline-first
  privacy-first
  a4
  webview
  jspdf
  storage-access-framework
  java
  android-app
  ```

- Enable **Releases**.
- Enable **Issues**.

---

## 2. Upload the source cleanly

Open a terminal inside the `Project-Mayhem-Android` folder and run:

```bash
git init
git branch -M main
git add .
git commit -m "Launch Project Mayhem Version 1"
git remote add origin https://github.com/AbyBijo/Project-Mayhem.git
git push -u origin main
```

Before `git add .`, verify that these private files are not being committed:

```text
keystore.properties
*.jks
local.properties
.gradle/
build/
```

Check with:

```bash
git status --ignored
```

> **Never upload the release keystore or its passwords.** Keep the signing key in a secure offline backup. Every future APK update must use the same key.

---

## 3. Make the repository visually strong

### Upload the social preview

A ready-made image is included at:

```text
docs/images/project-mayhem-social-preview.png
```

On GitHub:

1. Open **Settings**.
2. Find **Social preview** under the repository's general settings.
3. Click **Edit** or **Upload an image**.
4. Upload `project-mayhem-social-preview.png`.
5. Save.

This image is displayed when the repository link is shared on social media, messaging apps, and websites.

### Pin the repository

1. Open [github.com/AbyBijo](https://github.com/AbyBijo).
2. Click **Customize your pins**.
3. Select `Project-Mayhem`.
4. Save.

### Use a strong one-line profile note

Suggested profile line:

> Building practical Android tools with offline-first design, focused UX, and privacy-respecting storage.

---

## 4. Verify the README presentation

The README is already structured with:

- Project branding and status badges
- Screenshots with captions
- Feature summary
- APK installation instructions
- One-time folder setup
- Complete panel walkthrough
- Markdown reference
- PDF export workflow
- Privacy and permission model
- Troubleshooting
- Source-build instructions
- Release process
- Developer contacts

After pushing, inspect it on both desktop and mobile GitHub. Confirm that:

- Every screenshot loads.
- The PDF guide link opens.
- The CI badge appears.
- Headings are easy to scan.
- No local machine paths appear.
- Email and GitHub links work.

---

## 5. Enable the Android build check

The repository contains:

```text
.github/workflows/android-ci.yml
```

After the first push:

1. Open the **Actions** tab.
2. Allow workflows if GitHub asks.
3. Open **Android CI**.
4. Confirm that lint and debug APK compilation pass.
5. If the workflow succeeds, the README's build badge becomes green.

The workflow intentionally builds a debug APK and does not need the private release signing key.

---

## 6. Decide the license before calling it open source

A public GitHub repository is visible, but it is not automatically open source. Choose deliberately:

- **MIT License:** simple and permissive.
- **Apache License 2.0:** permissive with an explicit patent grant.
- **GPLv3:** derivative distributions must remain under GPLv3.
- **No license:** others may view the code but do not receive permission to reuse it.

On GitHub, use **Add file → Create new file**, name it `LICENSE`, and select **Choose a license template**. Do not add a license badge to the README until a license has actually been committed.

---

## 7. Create the Version 1 release

### Prepare the files

Use the signed release APK and checksum:

```text
Project-Mayhem.apk
Project-Mayhem.apk.sha256
```

Verify them before upload:

```bash
sha256sum Project-Mayhem.apk
```

### Create and push the tag

```bash
git tag -a version-1 -m "Project Mayhem Version 1"
git push origin version-1
```

### Publish on GitHub

1. Open **Releases**.
2. Click **Draft a new release**.
3. Choose the `version-1` tag.
4. Set the release title to:

   ```text
   Project Mayhem — Version 1
   ```

5. Paste the prepared release notes from:

   ```text
   docs/release-notes-version-1.md
   ```

6. Attach:

   - `Project-Mayhem.apk`
   - `Project-Mayhem.apk.sha256`

7. Mark it as the latest release.
8. Publish the release.
9. Download the uploaded APK and install it on a test device one final time.

---

## 8. Protect the main branch

Go to **Settings → Branches** or **Settings → Rules → Rulesets** and create a rule for `main`.

Recommended settings:

- Require a pull request before merging.
- Require the Android CI status check.
- Block force pushes.
- Block branch deletion.
- Require conversation resolution.

For a one-developer repository, you can leave administrator bypass enabled so urgent fixes remain possible.

---

## 9. Configure community files

The project includes:

```text
.github/ISSUE_TEMPLATE/bug_report.yml
.github/ISSUE_TEMPLATE/feature_request.yml
.github/ISSUE_TEMPLATE/config.yml
.github/PULL_REQUEST_TEMPLATE.md
.github/CODEOWNERS
CONTRIBUTING.md
SECURITY.md
CHANGELOG.md
```

These files make bug reports consistent, improve pull requests, assign review ownership, explain security reporting, and show release history.

Test them after pushing:

1. Click **Issues → New issue**.
2. Confirm that Bug Report and Feature Request appear.
3. Start a test pull request and confirm the checklist appears.

---

## 10. Keep the repository attractive after launch

### For every update

1. Create a focused branch.
2. Update code and screenshots when the UI changes.
3. Update `CHANGELOG.md`.
4. Run tests and lint.
5. Open a pull request.
6. Merge only when Android CI is green.
7. Build and sign the new release with the original key.
8. Publish the APK, checksum, and clear release notes.

### Good commit messages

```text
feat: add document preset support
fix: preserve folder status after activity recreation
docs: update export walkthrough
security: tighten WebView navigation policy
build: update AndroidX WebKit
```

### Screenshot discipline

- Use the same device theme and orientation.
- Remove personal notifications and private content.
- Use realistic example text.
- Keep filenames descriptive.
- Update README captions when replacing an image.

---

## 11. Final launch checklist

- [ ] Repository name is exactly `Project-Mayhem`.
- [ ] About description and topics are configured.
- [ ] Social preview is uploaded.
- [ ] README images and PDF link work.
- [ ] No keystore, passwords, or `local.properties` are committed.
- [ ] Android CI is green.
- [ ] License decision is explicit.
- [ ] Issues are enabled and templates work.
- [ ] Main branch protection is enabled.
- [ ] Version 1 tag exists.
- [ ] Signed APK and checksum are attached to the release.
- [ ] Uploaded APK has been installed and tested.
- [ ] Repository is pinned on the developer profile.

---

## Copy-ready launch post

> **Project Mayhem — Version 1 is live.**  
> An offline-first Android Markdown editor for creating stylized, borderless A4 PDFs with live preview, images, watermarks, optional encryption, negative output, and direct saving to a folder you choose. No account. No Internet permission. No broad storage access.  
>  
> Source, APK, screenshots, and full guide:  
> https://github.com/AbyBijo/Project-Mayhem

---

**Developer:** [Aby Bijo](https://github.com/AbyBijo)  
**Email:** [abybijo1978@gmail.com](mailto:abybijo1978@gmail.com)  
**Academic email:** [abybijo2025bca@mac.edu.in](mailto:abybijo2025bca@mac.edu.in)
