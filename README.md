# Android camera dev environment

Minimal VS Code Dev Container environment for building Android/Kotlin applications on a Linux host.

## Included

- JDK 17
- Android SDK command-line tools
- Android platform SDK 36
- Android Build Tools 36.0.0
- Android Platform Tools (`adb`)
- Git / SSH client / common archive utilities

## Usage

1. Clone this repository on the Linux dev-container host.
2. Open it from VS Code via your remote connection.
3. Run **Dev Containers: Reopen in Container**.
4. In the container, verify:

   ```bash
   java -version
   sdkmanager --list_installed
   adb version
   ```

The application Gradle project itself is intentionally not included yet. Add it at the repository root in the next step and use the Gradle Wrapper (`./gradlew`) for builds.
