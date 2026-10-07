# 🌸 SmartTubeEx v32.14 ✨

Update delivery & release pipeline: SmartTubeEx now ships its own signed update manifests from CI, so in-app update checks point at this repo instead of upstream.

### 🌟 What's New

- **🔗 Self-Hosted Updates** — update checks now resolve to the SmartTubeEx release feed (`Praveensenpai/smarttubeex`) instead of the upstream SmartTubeNext URLs.
- **🤖 Automated Manifests** — the release workflow generates and publishes `smarttube_stable2.json` and `smarttube_stable.json` on every tag, matching the built APKs and changelog.
- **🧭 Corrected Source Links** — in-app "sources" and "releases" links now point at the SmartTubeEx repository and releases page.

### 📦 Assets

| APK | Best for |
| --- | --- |
| `arm64-v8a` | Modern TVs and streaming boxes |
| `armeabi-v7a` | Older 32-bit devices |
| `x86` | Emulators |
| `universal` | If you are unsure which to pick |

### 🛠 Install

```bash
adb install -r SmartTube_stable_32.14_universal.apk
```
