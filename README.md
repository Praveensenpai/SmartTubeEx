<div align="center">

# 🌸 SmartTubeEx (スマートチューブEx) ✨
### Advanced SmartTube Client with Real-Time Keyword & Channel Filtering

> **Take full command of your Android TV feed. Silence clickbait, spoilers, and repetitive channels before they hit your screen.**

[![Latest Release](https://img.shields.io/github/v/release/Praveensenpai/SmartTubeEx?style=for-the-badge&color=cba6f7)](https://github.com/Praveensenpai/SmartTubeEx/releases)
[![Platform](https://img.shields.io/badge/Platform-Android%20TV-FCC624?style=for-the-badge&logo=android&logoColor=black)](https://github.com/Praveensenpai/SmartTubeEx)
[![Architecture](https://img.shields.io/badge/Arch-armeabi--v7a%20%7C%20arm64--v8a%20%7C%20x86-94e2d5?style=for-the-badge)](https://github.com/Praveensenpai/SmartTubeEx/releases)
[![License](https://img.shields.io/badge/License-MIT-89b4fa?style=for-the-badge)](LICENSE)

[📦 Download APKs](#-quick-download) • [✨ Key Enhancements](#-key-enhancements) • [🔄 Filtering Engine](#-filtering-architecture) • [🪄 ADB Install](#-one-liner-adb-install) • [⚙️ Configuration](#%EF%B8%8F-how-to-use-keyword-filtering)

</div>

<br>

> [!TIP]
> **Zero Bloat · Real-Time Title Filtering · Retains Full SmartTube Features**  
> Built for Android TV and Google TV devices to keep your recommendations, searches, and subscription feeds clean and spoiler-free.

---

## 📦 Quick Download

Download the pre-compiled APK matching your TV or streaming device architecture directly from the [Latest Release](https://github.com/Praveensenpai/SmartTubeEx/releases/latest):

| Target Architecture | Device Examples | Download |
|---|---|:---:|
| **ARM 32-bit (`armeabi-v7a`)** *(Most Common)* | Chromecast with Google TV (HD/4K), Fire TV Stick, Mi Box, Sony Bravia TVs | [⬇️ Download APK](https://github.com/Praveensenpai/SmartTubeEx/releases/latest/download/SmartTube_stable_32.07_armeabi-v7a.apk) |
| **ARM 64-bit (`arm64-v8a`)** | Nvidia Shield TV Pro (64-bit OS), Pixel Tablet, Flagship TV boxes | [⬇️ Download APK](https://github.com/Praveensenpai/SmartTubeEx/releases/latest/download/SmartTube_stable_32.07_arm64-v8a.apk) |
| **Universal (`all-in-one`)** | Any Android TV device (contains all native ABIs) | [⬇️ Download APK](https://github.com/Praveensenpai/SmartTubeEx/releases/latest/download/SmartTube_stable_32.07_universal.apk) |
| **x86 32-bit (`x86`)** | Android TV x86 emulators, Android-x86 PCs | [⬇️ Download APK](https://github.com/Praveensenpai/SmartTubeEx/releases/latest/download/SmartTube_stable_32.07_x86.apk) |

---

## ✨ Key Enhancements

| Feature | Description |
|---|---|
| **🚫 User Keyword Filter** | Define custom keywords or phrases to automatically hide matching video titles across Home, Search, Subscriptions, and Playlists. |
| **🛡️ Global Channel Blacklist** | Long-press any video card to blacklist the creator. Blacklisted channels disappear instantly from search results and feeds. |
| **⚡ Ultra-Fast Memory Engine** | In-memory keyword matching keeps title inspection fast enough for low-powered TV chips, with no network round-trips per video. |
| **💾 Backup & Sync Support** | Blocked keywords and blacklisted channels seamlessly export and import with SmartTube's built-in backup and restore utilities. |
| **🪄 All Native SmartTube Power** | Includes SponsorBlock, 8K/60fps/HDR support, background audio, ad-free playback, live chat, and customizable player controls. |

---

## 🔄 Filtering Architecture

```text
┌────────────────────────────────────────────────────────┐
│                   📺 Android TV Feed                   │
│    (Home Feed · Subscriptions · Search · Playlists)    │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
           ┌─────────────────────────────────┐
           │   🔍 VideoGroup Filter Gate     │
           │   · Case-insensitive regex/norm │
           │   · Checks Blocked Word Cache   │
           └────────────────┬────────────────┘
                            │
             ┌──────────────┴──────────────┐
             │                             │
             ▼ Match                       ▼ Clean
    ┌─────────────────┐           ┌─────────────────┐
    │  🚫 Filter Out  │           │  📺 Display on  │
    │  (Drop Video)   │           │    TV Screen    │
    └─────────────────┘           └─────────────────┘
```

---

## 🪄 One-Liner ADB Install

Install or update directly to your TV over Wi-Fi without leaving your workstation terminal:

```bash
# 1. Connect to your TV (replace with your TV's local IP)
adb connect 192.168.1.35:5555

# 2. Download and stream install directly
curl -L -o /tmp/smarttube_ex.apk https://github.com/Praveensenpai/SmartTubeEx/releases/latest/download/SmartTube_stable_32.07_armeabi-v7a.apk \
  && adb -s 192.168.1.35:5555 install -r /tmp/smarttube_ex.apk \
  && rm -f /tmp/smarttube_ex.apk
```

---

## ⚙️ How to Use Keyword Filtering

1. Open **Settings** (gear icon on the left sidebar).
2. Select **General** → **Keyword filter**.
3. Choose **Add keyword** and enter any term or phrase you want to exclude (e.g. `spoiler`, `prank`, `shorts`).
4. Select **Blocked keywords** to view or remove existing rules at any time.

---

## 🛡️ How to Block Channels

1. Highlight any video on your screen.
2. **Long-press the OK / Select / Center button** on your remote to open the **Context Menu**.
3. Click **Block channel** (or *Blacklist channel*).
4. The channel will be hidden immediately. To manage or unblock channels, open the **Blocked channels** tab on the left sidebar.

---

## 🛠️ Building From Source

```bash
# Clone the repository with submodules
git clone --recurse-submodules https://github.com/Praveensenpai/SmartTubeEx.git
cd SmartTubeEx

# Build the APKs using JDK 17
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleStstableDebug
```

Compiled APKs will be located in:
`smarttubetv/build/outputs/apk/ststable/debug/`

---

## 📜 License & Credits

- Upstream client by [yuliskov/SmartTube](https://github.com/yuliskov/SmartTube).
- Keyword filter implementation adapted from [XiveZ (PR #5796)](https://github.com/yuliskov/SmartTube/pull/5796).
- Licensed under the [MIT License](LICENSE).
