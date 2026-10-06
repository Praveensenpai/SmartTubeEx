# 🌸 SmartTubeEx v32.11 ✨

A player-feel pass: on-screen volume, controls that get out of the way, a position counter, and a cleaner quality picker.

### 🌟 What's New

- **🔊 Volume Slider On Screen** — changing volume now shows a bar overlay with a mute icon instead of a toast. It fades out on its own after a moment.
- **👻 Controls Hide While Paused** — the player UI now auto-hides when paused, just like during playback, so a paused frame stays clean.
- **🔢 Video Counter** — the player shows your position in the current list (e.g. `3 / 12`). It stays hidden for single videos.
- **🎚 One Row Per Resolution** — the quality list now shows a single row for each resolution (1080p, 720p, …) instead of a separate row for every codec. The active codec is shown right on the row.
- **🖐 Long-Press To Pick A Codec** — hold a resolution to open codec choices for that resolution only (AV1 / VP9 / AVC). Tapping the row applies the best available codec.
- **🔐 Signed & Verified** — every APK is signed with the project release keystore, and the build verifies the signature before publishing.

### 📦 Assets

| APK | Best for |
| --- | --- |
| `arm64-v8a` | Modern TVs and streaming boxes |
| `armeabi-v7a` | Older 32-bit devices |
| `x86` | Emulators |
| `universal` | If you are unsure which to pick |

### 🛠 Install

```bash
adb install -r SmartTube_stable_32.11_universal.apk
```
