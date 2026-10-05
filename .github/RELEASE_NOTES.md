# 🌸 SmartTubeEx v32.09 ✨

Autoplay now follows YouTube's real suggestions from feed rows, and the channel/keyword filter is lighter.

### 🌟 What's New

- **▶️ Correct Next Video From Feeds** — opening a video from Home, Search, Subscriptions, Trending, Music, News, Gaming, Sports, Movies, Live or Kids now continues with YouTube's real suggestions instead of the next card in that same row.
- **📃 Section Playlist Kept For Real Lists** — channel uploads, channel content and user playlists still auto-continue as before, so genuine ordered lists behave unchanged.
- **🧹 Leaner Filter Path** — removed the dead `VideoFilter` class and corrected the O(1) filter documentation.
- **🔑 Blocked Channel Matching Fixed** — blocked channels stored without a display name now match by id in the `equals` fallback.
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
adb install -r SmartTube_stable_32.09_universal.apk
```
