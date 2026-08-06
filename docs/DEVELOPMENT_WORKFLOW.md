# Silo Android Development & Release Workflow

This document outlines the official engineering workflow, branching strategy, build variant setup, feature flag architecture, CI/CD pipelines, and Definition of Done for the Silo Android Launcher repository.

---

## 1. Git Branch Strategy

We use a modified Gitflow strategy designed for safety, clean releases, and minimal merge conflicts.

### Branches

- **`main`**
  - **Purpose**: Production-ready code only. Every commit on `main` MUST be releasable.
  - **Restrictions**: Direct pushes are strictly disabled. Changes enter `main` only via Pull Requests from `develop` after passing all automated CI checks and code reviews.

- **`develop`**
  - **Purpose**: Integration branch for the upcoming release. All completed features and bug fixes merge into `develop` first.
  - **Restrictions**: Must remain buildable and pass unit tests at all times. Direct pushes are discouraged.

- **`feature/*`**
  - **Purpose**: Short-lived branches for individual features, enhancements, or bug fixes.
  - **Naming Examples**:
    - `feature/creator-mode`
    - `feature/focus-timer`
    - `feature/search-redesign`
    - `feature/widget-page`

### Workflow Flow

```
feature/*
   ↓ (Pull Request + Code Review + CI Checks)
develop
   ↓ (Internal Testing & Validation)
main
   ↓ (Automated Production Release Workflow)
GitHub Release / Play Store Release
```

---

## 2. Android Build Variants & Package Names

The project configures two product flavors under the `environment` dimension:

| Variant | Package Name (`applicationId`) | Version Suffix | App Identity | Preferences File | Labs Screen | Log Level |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Development (`dev`)** | `com.grout.silo.dev` | `-dev` | Launcher Dev | `launcher_dev` | Enabled | Verbose, Debug, Performance |
| **Production (`prod`)** | `com.grout.silo` | *(none)* | Silo | `launcher_prod` | Excluded | Warn & Error Only |

> [!NOTE]
> Both `dev` and `prod` builds have distinct package names (`com.grout.silo.dev` vs `com.grout.silo`) and separate Preference storage files (`launcher_dev` vs `launcher_prod`). This allows developers and internal testers to install both builds side-by-side on the same physical or virtual Android device without state collision.

---

## 3. App Identity

- **Development Build**:
  - App Name: `Launcher Dev`
  - Displayed in Launcher drawer and home screen as `Launcher Dev`.
- **Production Build**:
  - App Name: `Silo`

---

## 4. Feature Flag System & Storage

To support safe feature development without long-lived feature branches, experimental or incomplete features MUST be wrapped behind feature flags.

### Storage Architecture
- Feature flag states are managed by `FeatureFlagRepository` (`com.example.core.flag.FeatureFlagRepository`).
- Values are persisted asynchronously using **Android Jetpack DataStore Preferences** (`androidx.datastore.preferences`).
- States persist across app restarts and device reboots.

### Active Feature Flags (`FeatureFlag` Enum)

- `SAMPLE_EXPERIMENTAL_FEATURE`: Template flag for guarding upcoming features during feature development.
- New feature flags should be added directly to `FeatureFlag.kt` as enum entries when developing experimental functionality.

### Repository API

```kotlin
val repository = FeatureFlagRepository(context)

// Observe flag state continuously
val isEnabledFlow: Flow<Boolean> = repository.isEnabled(FeatureFlag.SAMPLE_EXPERIMENTAL_FEATURE)

// One-shot check
val isEnabled: Boolean = repository.isFeatureEnabled(FeatureFlag.SAMPLE_EXPERIMENTAL_FEATURE)

// Update flag state
repository.setEnabled(FeatureFlag.SAMPLE_EXPERIMENTAL_FEATURE, true)

// Reset all flags to defaults
repository.resetAll()
```

---

## 5. Labs Screen (Development Only)

Development builds include a hidden **Silo Labs** screen accessible via **Settings -> Silo Labs (Developer)**.

### Capabilities
- Toggle feature flags in real-time without recompiling the app.
- **Reset All Feature Flags**: Instantly reverts all flags to default values.
- **Reset Launcher Preferences**: Clears environment preference storage (`launcher_dev`).
- **Build Info Inspector**: Inspect `VERSION_NAME`, `VERSION_CODE`, `BUILD_TYPE`, `FLAVOR`, package name, and preferences file path.

> [!IMPORTANT]
> The `LabsScreen` implementation is placed inside the `src/dev` source set. Production builds (`src/prod`) compile a no-op placeholder, ensuring internal developer tools and feature flag controls never ship to end users.

---

## 6. Build & Environment Configuration

Environment configuration is governed by `AppConfig` (`com.example.core.config.AppConfig`):

- **`DevConfig` (`src/dev`)**:
  - Environment: `Development`
  - Preferences Name: `launcher_dev`
  - Debug Logging: Enabled (`Log.d`, `Log.v`, `Log.p` performance timer)
  - Labs Screen: Available
  - Analytics & Crash Reporting: Disabled

- **`ProdConfig` (`src/prod`)**:
  - Environment: `Production`
  - Preferences Name: `launcher_prod`
  - Debug Logging: Disabled (only `Log.w` and `Log.e`)
  - Labs Screen: Unavailable
  - Analytics & Crash Reporting: Enabled

---

## 7. Versioning Policy

We adhere strictly to **Semantic Versioning (SemVer)**: `MAJOR.MINOR.PATCH`

Examples:
- `1.0.0`: Initial production release
- `1.1.0`: Feature addition (backwards compatible)
- `1.1.1`: Patch / Hotfix release
- `2.0.0`: Major architectural revision

Development builds append the suffix: `1.2.0-dev`.

---

## 8. Target Module Structure

The project is structured to remain modular and scalable:

```
silo-launcher-android/
├── app/                  # Main application module (activities, UI, DI)
├── core/                 # Core domain models, logging, data repositories, feature flags
│   ├── config/           # AppConfig, DevConfig, ProdConfig
│   ├── flag/             # FeatureFlag, FeatureFlagRepository
│   └── log/              # AppLogger
├── designsystem/         # UI theme, typography, common design components
├── feature/              # Independent feature modules
│   ├── home/
│   ├── focus/
│   ├── creator/
│   ├── search/
│   ├── settings/
│   └── widgets/
├── docs/                 # Workflow, architecture, and developer documentation
└── .github/              # GitHub Actions workflows (CI/CD)
```

---

## 9. Pull Request Rules & GitHub Actions

### CI Workflows

1. **Pull Request Check (`.github/workflows/pr.yml`)**:
   - Triggers on PRs targeting `develop` or `main`.
   - Executes:
     - `./gradlew assembleDevDebug assembleProdDebug`
     - `./gradlew testDevDebugUnitTest testProdDebugUnitTest`
     - `./gradlew lint`

2. **Production Release Workflow (`.github/workflows/release.yml`)**:
   - Triggers on push / merge to `main` or version tags (`v*`).
   - Executes:
     - Generates Release APK (`assembleProdRelease`)
     - Generates Release AAB (`bundleProdRelease`)
     - Creates GitHub Release with build artifacts attached.

### Pull Request Mandates

Every Pull Request MUST:
1. Build successfully in both `dev` and `prod` variants.
2. Pass all unit tests (`./gradlew test`).
3. Pass Android Lint check (`./gradlew lint`).
4. Be reviewed and approved by at least one maintainer.

---

## 10. Feature Development Policy

1. **Create Branch**: Create a short-lived `feature/*` branch from `develop`.
2. **Implement Feature**: Develop the feature.
3. **Feature Flag Guard**: Protect unfinished or experimental functionality behind a `FeatureFlag`.
4. **Merge to `develop`**: Open a PR targeting `develop`.
5. **Internal Test**: Test thoroughly in the `dev` build variant using the Labs screen.
6. **Enable for Production**: Enable feature default in production only when fully tested and signed off.

---

## 11. Definition of Done (DoD)

A feature is considered **Done** only when:
- [x] Builds cleanly in `dev` and `prod` variants.
- [x] Has zero lint errors (`./gradlew lint`).
- [x] Unit tests pass (`./gradlew test`).
- [x] Feature flag added and tested (if experimental).
- [x] Development build verified on device or emulator.
- [x] Code reviewed and approved via Pull Request.
- [x] Merged into `develop`.
