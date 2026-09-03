# Silo Android Release Guide

This document provides a comprehensive, step-by-step reference for managing, building, signing, and deploying production releases for the **Silo Android Launcher**.

---

## 📋 Table of Contents
- [1. Release Architecture & Overview](#1-release-architecture--overview)
- [2. Phase 1: Pre-Release Setup & Key Management](#2-phase-1-pre-release-setup--key-management)
  - [2.1 Android Upload Keystore](#21-android-upload-keystore)
  - [2.2 GitHub Repository Secrets](#22-github-repository-secrets)
  - [2.3 Environment & Local Prerequisites](#23-environment--local-prerequisites)
- [3. Phase 2: Activating & Managing the Self-Hosted Runner](#3-phase-2-activating--managing-the-self-hosted-runner)
  - [3.1 Runner Prerequisites](#31-runner-prerequisites)
  - [3.2 Obtaining a Registration Token](#32-obtaining-a-registration-token)
  - [3.3 Launching the Runner](#33-launching-the-runner)
  - [3.4 Runner Lifecycle Management Commands](#34-runner-lifecycle-management-commands)
- [4. Phase 3: Version Upgrade & Tagging](#4-phase-3-version-upgrade--tagging)
  - [4.1 Semantic Versioning Policy](#41-semantic-versioning-policy)
  - [4.2 Automated Version Bumping (Recommended)](#42-automated-version-bumping-recommended)
  - [4.3 Manual Version Bumping](#43-manual-version-bumping)
- [5. Phase 4: Release Execution Strategies](#5-phase-4-release-execution-strategies)
  - [Strategy A: Tag-Triggered Automated Release (Standard Flow)](#strategy-a-tag-triggered-automated-release-standard-flow)
  - [Strategy B: On-Demand Manual GitHub Release (Workflow Dispatch)](#strategy-b-on-demand-manual-github-release-workflow-dispatch)
  - [Strategy C: Local Emergency Release Build](#strategy-c-local-emergency-release-build)
- [6. Phase 5: Post-Release Tasks](#6-phase-5-post-release-tasks)
  - [6.1 Verify GitHub Release Artifacts](#61-verify-github-release-artifacts)
  - [6.2 Device Smoke Testing](#62-device-smoke-testing)
  - [6.3 Google Play Store Deployment](#63-google-play-store-deployment)
  - [6.4 Git Branch Synchronization](#64-git-branch-synchronization)
  - [6.5 Self-Hosted Runner Teardown](#65-self-hosted-runner-teardown)
- [7. Troubleshooting & FAQ](#7-troubleshooting--faq)

---

## 1. Release Architecture & Overview

The release pipeline produces two primary build artifacts for the `prod` flavor:
- **APK (`app-prod-release.apk`)**: Standalone application binary for direct installation, GitHub Releases, and manual testing.
- **Android App Bundle (`app-prod-release.aab`)**: Optimized publishing format required for Google Play Store submission.

```
                  ┌────────────────────────┐
                  │   bump-version.sh      │
                  │ (Update version & tag) │
                  └───────────┬────────────┘
                              │
                              ▼
                  ┌────────────────────────┐
                  │ git push origin v1.X.X │
                  └───────────┬────────────┘
                              │
                              ▼
            ┌────────────────────────────────────┐
            │   GitHub Actions (release.yml)     │
            │   Runs on: self-hosted runner      │
            └─────────────────┬──────────────────┘
                              │
       ┌──────────────────────┴──────────────────────┐
       ▼                                             ▼
┌───────────────┐                             ┌───────────────┐
│ Decode Keys & │                             │  GitHub CLI / │
│ Gradle Build  │                             │ Softprops Release │
└──────┬────────┘                             └───────┬───────┘
       │                                              │
       ▼                                              ▼
┌─────────────────────────────────────────────────────────────┐
│ Attach APK & AAB to GitHub Release v1.X.X                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Phase 1: Pre-Release Setup & Key Management

### 2.1 Android Upload Keystore

Production builds require signing with a private Java Keystore (`.jks`).

1. **Keystore File**: `my-upload-key.jks` (stored at project root during build execution; excluded from git via `.gitignore`).
2. **Base64 Representation**: `keystore_b64.txt` allows storing the encrypted keystore as a string secret.
3. **Generating / Encoding Keystore**:
   To encode an existing `my-upload-key.jks` into base64 for secrets configuration:
   - **PowerShell (Windows)**:
     ```powershell
     [Convert]::ToBase64String([IO.File]::ReadAllBytes("my-upload-key.jks")) | Set-Content keystore_b64.txt
     ```
   - **Bash (Linux/macOS)**:
     ```bash
     base64 -w 0 my-upload-key.jks > keystore_b64.txt
     ```

> [!CAUTION]
> Never commit `my-upload-key.jks` or `keystore_b64.txt` with sensitive passwords to public version control. Ensure `.gitignore` includes `*.jks` and `keystore_b64.txt`.

---

### 2.2 GitHub Repository Secrets

The GitHub Actions workflow requires the following secrets configured under **Repository Settings -> Secrets and variables -> Actions**:

| Secret Name | Description | Example / Format |
| :--- | :--- | :--- |
| `KEYSTORE_B64` | Base64 encoded string of `my-upload-key.jks` | Long single-line base64 string |
| `STORE_PASSWORD` | Keystore password | Confidential password |
| `KEY_PASSWORD` | Key alias password | Confidential password |
| `GITHUB_TOKEN` | Automatically provided by GitHub Actions | Managed by GitHub |

---

### 2.3 Environment & Local Prerequisites

If executing builds locally or maintaining the runner machine, verify:
- **JDK 17** installed and set as `JAVA_HOME`.
- **Android SDK** (API Level 34 / Build Tools 34.0.0+).
- **Docker Desktop** (if using the self-hosted containerized runner).
- **Git & PowerShell 7+** (Windows) or **Bash** (macOS/Linux).

---

## 3. Phase 2: Activating & Managing the Self-Hosted Runner

The GitHub release workflow targets `runs-on: self-hosted`. A PowerShell helper script [`gh-selfhosted-runner.ps1`](file:///e:/Study/silo/silo-launcher-android/gh-selfhosted-runner.ps1) manages a containerized runner instance based on `myoung34/github-runner:latest`.

---

### 3.1 Runner Prerequisites
- Docker Engine / Docker Desktop running on the runner host machine.
- Internet connectivity to `github.com`.

---

### 3.2 Obtaining a Registration Token
Before starting a new runner container, obtain a registration token:
1. Go to GitHub Repository: **Settings -> Actions -> Runners**.
2. Click **New self-hosted runner**.
3. Copy the registration token shown in the configuration steps.

---

### 3.3 Launching the Runner
Execute the PowerShell helper script with your runner token:

```powershell
.\gh-selfhosted-runner.ps1 -Action start -Token "YOUR_GITHUB_RUNNER_TOKEN"
```

*This command will:*
- Stop any existing runner container (`silo-github-runner`).
- Launch a fresh background container with automatic restart policy enabled (`--restart always`).
- Print the initial runner startup logs to confirm registration with GitHub.

---

### 3.4 Runner Lifecycle Management Commands

Use [`gh-selfhosted-runner.ps1`](file:///e:/Study/silo/silo-launcher-android/gh-selfhosted-runner.ps1) to monitor and manage the active runner:

- **Check Runner Status**:
  ```powershell
  .\gh-selfhosted-runner.ps1 status
  ```
- **View Live Logs**:
  ```powershell
  .\gh-selfhosted-runner.ps1 logs
  ```
- **Restart Runner**:
  ```powershell
  .\gh-selfhosted-runner.ps1 restart
  ```
- **Stop & Remove Runner**:
  ```powershell
  .\gh-selfhosted-runner.ps1 stop
  ```

---

## 4. Phase 3: Version Upgrade & Tagging

### 4.1 Semantic Versioning Policy
Version numbers follow **SemVer** (`MAJOR.MINOR.PATCH`):
- `versionName`: Defined in [`app/build.gradle.kts`](file:///e:/Study/silo/silo-launcher-android/app/build.gradle.kts) (e.g. `1.1.0`).
- `versionCode`: Dynamically set during CI release builds using `github.run_number` to guarantee monotonically increasing version codes for Google Play.

---

### 4.2 Automated Version Bumping (Recommended)

Use the bash script [`bump-version.sh`](file:///e:/Study/silo/silo-launcher-android/bump-version.sh) to handle version updates, git commits, and tagging atomically.

#### Step 1: Run the Script
In Git Bash, WSL, or macOS Terminal:
```bash
./bump-version.sh 1.1.0
```
*(If no argument is provided, the script will prompt interactively for the version).*

#### What the Script Does:
1. Strips leading `v` prefixes (converts `v1.1.0` -> `1.1.0`).
2. Updates `versionName = "1.1.0"` inside `app/build.gradle.kts`.
3. Creates git commit: `chore(release): bump version to 1.1.0`.
4. Creates git tag: `v1.1.0`.

#### Step 2: Push Commits and Tag
Follow the output instructions from the script:
```bash
# 1. Push code changes / PR to main branch
git push origin HEAD

# 2. Push the release tag to trigger CI build
git push origin v1.1.0
```

---

### 4.3 Manual Version Bumping

If updating manually without `bump-version.sh`:

1. Open [`app/build.gradle.kts`](file:///e:/Study/silo/silo-launcher-android/app/build.gradle.kts) and update:
   ```kotlin
   versionName = "1.1.0"
   ```
2. Commit the change:
   ```bash
   git add app/build.gradle.kts
   git commit -m "chore(release): bump version to 1.1.0"
   ```
3. Create an annotated git tag:
   ```bash
   git tag -a v1.1.0 -m "Release v1.1.0"
   ```
4. Push code and tag:
   ```bash
   git push origin main
   git push origin v1.1.0
   ```

---

## 5. Phase 4: Release Execution Strategies

### Strategy A: Tag-Triggered Automated Release (Standard Flow)

This is the primary production deployment method.

1. Ensure the self-hosted runner container is active (`.\gh-selfhosted-runner.ps1 status`).
2. Push the version tag to GitHub (`git push origin v1.1.0`).
3. GitHub Actions triggers `.github/workflows/release.yml`.
4. The workflow:
   - Decodes `secrets.KEYSTORE_B64`.
   - Injects CI run number into `versionCode`.
   - Executes `./gradlew assembleProdRelease bundleProdRelease`.
   - Generates release artifacts.
   - Publishes a new release on GitHub Releases with binaries attached.

---

### Strategy B: On-Demand Manual GitHub Release (Workflow Dispatch)

To trigger a production release manually via the GitHub UI without pushing a new tag:

1. Navigate to GitHub Repository -> **Actions**.
2. Select **Production Release** from the workflows list on the left sidebar.
3. Click the **Run workflow** dropdown button.
4. Select the target branch (`main`).
5. Click **Run workflow**.

> [!NOTE]
> When run via `workflow_dispatch` without a tag ref, the workflow automatically extracts `versionName` directly from `app/build.gradle.kts` and constructs tag `v<versionName>`.

---

### Strategy C: Local Emergency Release Build

If GitHub infrastructure or runner is offline, use [`local-release.ps1`](file:///e:/Study/silo/silo-launcher-android/local-release.ps1) for direct local production builds:

```powershell
.\local-release.ps1 -VersionTag v1.1.0
```

#### What the Local Release Script Performs:
1. Prompts for `VersionTag` if not provided.
2. Checks for `my-upload-key.jks`; automatically decodes `keystore_b64.txt` if necessary.
3. Prompts securely for `STORE_PASSWORD` and `KEY_PASSWORD`.
4. Executes Gradle build: `.\gradlew.bat assembleProdRelease bundleProdRelease --no-daemon`.
5. Outputs generated APK and AAB files to:
   - `app\build\outputs\apk\prod\release\`
   - `app\build\outputs\bundle\prodRelease\`
6. Creates local git tag `$VersionTag` and pushes to origin.
7. If GitHub CLI (`gh`) is installed locally, automatically creates a GitHub Release and uploads artifacts.

---

## 6. Phase 5: Post-Release Tasks

### 6.1 Verify GitHub Release Artifacts
1. Go to Repository -> **Releases**.
2. Select tag `v1.1.0`.
3. Verify the release includes:
   - `app-prod-release.apk`
   - `app-prod-release.aab`
   - Automated release notes / changelog.

---

### 6.2 Device Smoke Testing
1. Download `app-prod-release.apk` from the GitHub Release page.
2. Install on a physical test Android device:
   ```bash
   adb install -r app-prod-release.apk
   ```
3. Perform basic smoke testing:
   - Launcher startup & default home screen layout.
   - Settings accessibility.
   - Verify Silo Labs is hidden in `prod` build.

---

### 6.3 Google Play Store Deployment
1. Log into [Google Play Console](https://play.google.com/console).
2. Select **Silo Launcher**.
3. Navigate to **Testing -> Internal testing** or **Production**.
4. Click **Create new release**.
5. Upload `app-prod-release.aab` generated during the build.
6. Enter release notes and submit for review.

---

### 6.4 Git Branch Synchronization
Sync the release commit back to `develop` to ensure version bump stays in sync across integration branches:

```bash
git checkout develop
git pull origin develop
git merge main
git push origin develop
```

---

### 6.5 Self-Hosted Runner Teardown
If using a temporary local runner machine, stop the container post-release:

```powershell
.\gh-selfhosted-runner.ps1 stop
```

---

## 7. Troubleshooting & FAQ

### Q1: The release workflow failed with `keystore decoding failed` or `password incorrect`
- **Solution**: Re-encode `my-upload-key.jks` using `[Convert]::ToBase64String(...)` without trailing line breaks and update the `KEYSTORE_B64` secret in GitHub Repository settings. Verify `STORE_PASSWORD` and `KEY_PASSWORD`.

### Q2: Gradle build failed due to `versionCode` collision on Google Play
- **Solution**: The workflow automatically sets `versionCode = ${{ github.run_number }}`. If publishing manually via `local-release.ps1`, ensure `versionCode` in `app/build.gradle.kts` is incremented higher than the last published Play Store release.

### Q3: Self-hosted runner shows offline in GitHub UI
- **Solution**: Run `.\gh-selfhosted-runner.ps1 status` and `.\gh-selfhosted-runner.ps1 logs`. If the container stopped or registration token expired, restart using `.\gh-selfhosted-runner.ps1 restart` or re-register with a new token from GitHub Settings.

### Q4: Build artifacts missing from GitHub release page
- **Solution**: Check workflow logs under the **Create GitHub Release** step. Verify `softprops/action-gh-release@v1` had permissions (`permissions: contents: write` in `.github/workflows/release.yml`).
