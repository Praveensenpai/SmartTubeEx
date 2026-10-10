# 🌸 SmartTubeEx v32.15 ✨

Live playback position and total duration are now exposed to the Android media session, so TV companion apps and notifiers can show real progress.

### 🌟 What's New

- **⏱ Live Progress in the Media Session** — SmartTube now mirrors `pos=<ms>;dur=<ms>` into the media session metadata, refreshed every second while playing.
- **📡 Companion App Ready** — external notifiers (e.g. TEREBI) can read accurate elapsed and total time directly from `dumpsys media_session`, which never prints a `duration=` field on its own.
- **🔋 Light Touch** — a single 1-second handler refreshes playback state and metadata; it is removed on view teardown with no leaks.

### 📦 Assets

| APK | Best for |
| --- | --- |
| `arm64-v8a` | Modern TVs and streaming boxes |
| `armeabi-v7a` | Older 32-bit devices |
| `x86` | Emulators |
| `universal` | If you are unsure which to pick |

### 🛠 Install

```bash
adb install -r SmartTube_stable_32.15_universal.apk
```