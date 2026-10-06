# 🌸 SmartTubeEx v32.13 ✨

Fast startup video loading optimization: reduced initial playback buffer to 500ms for near-instant video start while maintaining gradual 30s–100s buffer ahead in the background.

### 🌟 What's New

- **⚡ Instant Video Playback Start** — decreased initial buffer threshold (`bufferForPlaybackMs`) from 2,500ms down to 500ms. Playback begins almost immediately without the prolonged spinner wait on initial video click.
- **🔄 Gradual Background Buffer** — once playback begins, ExoPlayer continues steadily streaming chunks in the background up to the configured 30s–100s limit to ensure rock-solid, stall-free viewing.
- **⏱️ Rapid Rebuffer Recovery** — shortened post-rebuffer pause threshold (`bufferForPlaybackAfterRebufferMs`) to 2,000ms for quick resumption if network hiccups.

### 📦 Assets

| APK | Best for |
| --- | --- |
| `arm64-v8a` | Modern TVs and streaming boxes |
| `armeabi-v7a` | Older 32-bit devices |
| `x86` | Emulators |
| `universal` | If you are unsure which to pick |

### 🛠 Install

```bash
adb install -r SmartTube_stable_32.13_universal.apk
```
