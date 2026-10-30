# Dev ContainerからAndroid実機へADB接続する手順

## 環境

- 開発環境：Linuxホスト上のDocker ComposeベースDev Container
- Android SDK / ADB：`/opt/android-sdk/platform-tools/adb`
- 実機：Huawei JDN2-W09NM（Android 9）
- タブレットIP：`192.168.1.68`
- 接続方式：ADB over TCP/IP（ポート5555）

Android 9のため、ADBのTCP/IPモードを最初にUSB経由で有効化する。

## 1. タブレット側の準備

1. 「設定」→「タブレット情報」からビルド番号を7回タップし、開発者向けオプションを有効化する。
2. 開発者向けオプションで「USBデバッグ」をONにする。
3. **「充電専用モードでUSBデバッグを許可」をONにする。**
4. タブレットをLinuxホストへUSB接続する。

### タブレット固有の注意点

今回の実験で使用した機種では、「充電専用モードでUSBデバッグを許可」がOFFの場合、USBケーブルを抜くと「USBデバッグ」のスイッチもOFFになる挙動が確認された。
この設定をONにすると、以下の状態でもUSBデバッグのスイッチがONのまま維持された。

- USBケーブルを抜いた状態
- USBケーブルがPCに接続されていない状態
- 充電専用のUSB接続状態

**LAN経由ADBを安定して利用するため、この設定を有効にしておく。**

なお、USBデバッグのスイッチがONのままでも、ADBのTCP/IP待ち受けが端末再起動後に維持されるとは限らない。

## 2. USB経由でADB TCP/IPモードを有効化

Linuxホスト側で、USBデバイスをDev Containerへ渡すため、一時的に`compose.yml` の `dev` サービスに以下を設定した。

```yaml
volumes:
  - /dev/bus/usb:/dev/bus/usb

device_cgroup_rules:
  - 'c 189:* rwm'
```

Dev Containerを再作成した後、コンテナ内で実行する。
root権限でadbサーバを起動するのがポイント。

```bash
adb kill-server
sudo /opt/android-sdk/platform-tools/adb start-server
adb devices -l
```

```bash
$ adb devices -l
List of devices attached
GHR9xxxx       unauthorized usb:3-1 transport_id:1
```

`unauthorized`となっている場合、
タブレット側にUSBデバッグの認証ダイアログが表示されているはずなので、許可する。

`device` に変化したら命令を受け付ける状態なので、TCP/IPモードに切り替える。

```bash
$ adb devices -l
List of devices attached
GHR9xxxx       device usb:3-1 transport_id:1

$ adb tcpip 5555
```

期待する出力：

```text
restarting in TCP mode port: 5555
```

一旦、この状態になれば、あとはLAN経由でアクセス可能。

## 3. 通常ユーザーでLAN接続

rootのADBサーバを停止する。

```bash
sudo /opt/android-sdk/platform-tools/adb kill-server
```

通常ユーザー（`vscode`）で接続する。

```bash
adb connect 192.168.1.68:5555
adb devices -l
```

初回は `unauthorized` や `failed to authenticate` になる場合がある。
また、rootで接続した場合とはADB認証鍵が変わっているので、
タブレット側に表示されるADB認証ダイアログを再度許可する。

以下の状態になれば接続成功。

```text
192.168.1.68:5555  device
```

動作確認：

```bash
adb shell getprop ro.build.version.release
```

`9` が返れば成功。

## 4. 2回目以降の接続

タブレット側のADB TCP/IPモードが維持されていれば、USB接続による初期化は不要。

```bash
adb connect 192.168.1.68:5555
adb devices -l
```

タブレット再起動などでTCP/IPモードが解除された場合は、手順2からやり直す。

## 5. logcatによるログ取得

直近20行のログ：

```bash
adb logcat -d -t 20 -v time
```

リアルタイムログ監視：

```bash
adb logcat -v time
```

`Ctrl+C` で終了する。

## 注意事項

- ADB TCP/IPの5555番ポートは信頼できる宅内LAN内だけで使用し、インターネットへ公開しない。
- rootと通常ユーザーではADB認証鍵が異なるため、それぞれ認証が必要になる場合がある。
- タブレットをUSB接続した時に `no permissions` と表示される場合でも、root権限でADBサーバを起動することで、今回の環境では接続できた。
- 本実験で使用したタブレットでは「充電専用モードでUSBデバッグを許可」をONにすることで、USBケーブルを抜いてもUSBデバッグの設定を維持できることを確認した。
- タブレットのIPアドレスが変わった場合は、接続先を変更する。
