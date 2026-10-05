# 🌸 SmartTubeEx v32.08 ✨

Silent background updates and a clean sidebar — the "Update" entry no longer clutters your menu.

### 🌟 What's New

- **🤫 Silent Background Updates** — background update checks now finish quietly: no sidebar entry, no pop-up dialog.
- **⬆️ Manual Update Only** — the install dialog appears only when you explicitly tap *Check for updates* in Settings → About.
- **🎨 Cleaner Sidebar** — the sidebar is back to just your content; no update clutter between sections.
- **🔐 Signed & Verified** — every APK is signed with the project release keystore, and the build verifies the signature before publishing.

### ⚠️ Upgrading from v32.07 or earlier

This release is signed with a **new release key**. Android will refuse to install it over an older build, so uninstall the old version first:

```bash
adb uninstall org.smarttube.stable
adb install SmartTube_stable_32.08_universal.apk
```

(Or on the TV: Settings → Apps → SmartTube → Uninstall, then install the new APK.)

### 📦 Assets

| APK | Best for |
| --- | --- |
| `arm64-v8a` | Modern TVs and streaming boxes |
| `armeabi-v7a` | Older 32-bit devices |
| `x86` | Emulators |
| `universal` | If you are unsure which to pick |

Verify your download against `SHA256SUMS.txt`.