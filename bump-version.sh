#!/usr/bin/env bash
set -e

# Usage: ./bump-version.sh [VERSION]
# Example: ./bump-version.sh 1.1.0

NEW_VERSION="$1"

if [ -z "$NEW_VERSION" ]; then
  read -p "Enter new version (e.g. 1.1.0 or 2.0.0): " NEW_VERSION
fi

# Strip leading 'v' if typed (e.g. v1.1.0 -> 1.1.0)
NEW_VERSION="${NEW_VERSION#v}"

GRADLE_FILE="app/build.gradle.kts"

if [ ! -f "$GRADLE_FILE" ]; then
  echo "Error: $GRADLE_FILE not found!"
  exit 1
fi

echo "Updating versionName in $GRADLE_FILE to $NEW_VERSION..."

# Cross-platform sed update (macOS & Linux/Git Bash/WSL)
if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' "s/versionName = .*/versionName = \"$NEW_VERSION\"/" "$GRADLE_FILE"
else
  sed -i "s/versionName = .*/versionName = \"$NEW_VERSION\"/" "$GRADLE_FILE"
fi

# Stage and commit
git add "$GRADLE_FILE"
git commit -m "chore(release): bump version to $NEW_VERSION"
git tag "v$NEW_VERSION"

echo ""
echo "✅ Successfully updated version to $NEW_VERSION and created tag v$NEW_VERSION!"
echo ""
echo "Next steps:"
echo "  1. Push branch / open PR and merge to main:  git push origin HEAD"
echo "  2. Push the release tag:                      git push origin v$NEW_VERSION"
