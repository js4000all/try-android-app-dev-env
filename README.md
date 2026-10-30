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

# Android Camera Sample — Step 1

Android 9 (API 28) 以降で起動する、文字表示だけの最小Kotlin Androidアプリです。
既存のdev containerを利用し、Android Studioは不要です。

## 最初のビルド

Dev Containerで開いたターミナルから実行します。

```bash
bash scripts/bootstrap-gradle-wrapper.sh
./gradlew assembleDebug
```

初回スクリプトは公式のGradle 8.13を一時ダウンロードして、標準のGradle Wrapper (`gradlew` / `gradlew.bat` / `gradle/wrapper/*`) を生成します。**生成されたWrapperはGitにコミット**してください。次回以降、bootstrapは不要です。

APK: `app/build/outputs/apk/debug/app-debug.apk`

## 実機での確認

USBデバッグを有効にしたAndroid端末を`adb devices`で確認します。Docker内からUSBデバイスが見えない構成なら、APKをMacまたはAndroid端末へ転送してインストールしてください。

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## バージョン

- JDK 17 (dev container)
- Android Gradle Plugin 8.13.2
- Kotlin Android plugin 2.2.20
- Gradle 8.13
- compileSdk 36 / minSdk 28 / targetSdk 28

`targetSdk=28`はAndroid 9端末で最初のビルド・起動を確認するための暫定設定です。配布前には現行のAndroid要件および新しいOSでの動作を見直してください。

このサンプルはカメラ・マイク・ネットワーク通信・権限要求を含みません。

## ADB経由でAPKをインストール

事前に、[LAN経由でのADB接続を確立](docs/adb-setup.md)しておく。
これにより、Dev Containerホストにタブレットを直結しなくても良く、開発PCの前で作業できる。

```bash
# APKをビルド
./gradlew assembleDebug

# ADB接続を確認
adb devices -l

# APKをインストール
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

事前に消す場合。
```bash
adb uninstall local.example.androidcamerasample
```
