# 🌸 SmartTubeEx v32.12 ✨

A focused audio and playback release: filter audio languages to only those actually available for the playing video, eliminating clutter on single-track and multi-language content.

### 🌟 What's New

- **🎙️ Video-Specific Audio Languages** — the in-player audio language selector now only displays audio tracks actually present in the current video (e.g. Original, English, Spanish, Japanese). Hundreds of unavailable device locales are filtered out.
- **🧹 Clean Player Menu For Single-Track Videos** — if a video only has a single audio language with no alternatives, the audio language menu item is cleanly hidden, keeping the player interface lean.
- **⚙️ Preserved Global Language Settings** — global preferred audio language configuration remains fully accessible in app settings (*Settings → Player → Audio language*).

### 📦 Assets

| APK | Best for |
| --- | --- |
| `arm64-v8a` | Modern TVs and streaming boxes |
| `armeabi-v7a` | Older 32-bit devices |
| `x86` | Emulators |
| `universal` | If you are unsure which to pick |

### 🛠 Install

```bash
adb install -r SmartTube_stable_32.12_universal.apk
```
