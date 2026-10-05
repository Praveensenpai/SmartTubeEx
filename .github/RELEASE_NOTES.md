# 🌸 SmartTubeEx v32.10 ✨

Block a bad channel without leaving playback, plus the v32.09 autoplay fix.

### 🌟 What's New

- **🚫 Block Channel From The Player** — a new player toolbar button blocks (or unblocks) the channel of the video you are watching. No need to back out to the grid and long-press the card.
- **⚙️ Player Button Toggle** — the button is listed in Settings → Player → Player buttons, so you can place or hide it like any other control.
- **🧩 Shared Logic** — the player button reuses the existing `BlockedChannelData` store, so blocked channels disappear from all lists and the Blocked Channels sidebar section shows/hides exactly as before.
- **▶️ Correct Next Video From Feeds** — opening a video from Home, Search, Subscriptions, Trending, Music, News, Gaming, Sports, Movies, Live or Kids continues with YouTube's real suggestions instead of the next card in that same row.
- **📃 Section Playlist Kept For Real Lists** — channel uploads, channel content and user playlists still auto-continue as before.
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
adb install -r SmartTube_stable_32.10_universal.apk
```