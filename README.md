# Silo Android Launcher

Silo is a minimal, context-shifting Android launcher built with Jetpack Compose.

[![Privacy Policy](https://img.shields.io/badge/Privacy_Policy-Online-blue)](docs/PRIVACY_POLICY.md)

## Architectural Highlights & Build Flavors

This project supports a production-ready development workflow with:
- **Build Variants**:
  - `dev` (`com.grout.silo.dev`, App Name: `Launcher Dev`): For internal testing, verbose logging, and developer tools.
  - `prod` (`com.grout.silo`, App Name: `Silo`): For production release builds.
  - Both variants can be installed side-by-side on the same device without state conflict.
- **Feature Flags**: Persistent feature flags backed by Android DataStore Preferences.
- **Silo Labs**: Hidden developer screen (in `dev` build set only) to toggle feature flags, reset preferences, and inspect build parameters.
- **CI/CD Pipelines**: GitHub Actions workflows for PR check (build, unit tests, lint) and automated release packaging.
- **Privacy Policy**: Read our Google Play compliant [Privacy Policy](docs/PRIVACY_POLICY.md) (hosted via GitHub Pages).

For complete branch strategies, release rules, and Definition of Done, refer to [DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md).


## Build Instructions

```bash
# Build Development Debug APK (Launcher Dev)
./gradlew assembleDevDebug

# Build Production Debug APK (Silo)
./gradlew assembleProdDebug

# Run Unit Tests
./gradlew test

# Run Android Lint
./gradlew lint
```
