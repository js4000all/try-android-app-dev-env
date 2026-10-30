#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
VERSION=8.13
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
curl --fail --location --retry 3 --silent --show-error   "https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip"   -o "$TMP_DIR/gradle.zip"
unzip -q "$TMP_DIR/gradle.zip" -d "$TMP_DIR"
"$TMP_DIR/gradle-${VERSION}/bin/gradle" wrapper --gradle-version "$VERSION" --distribution-type bin
chmod +x gradlew
printf '\nGradle Wrapper generated. Run: ./gradlew assembleDebug\n'
