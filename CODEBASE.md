# CODEBASE.md: SmartTubeEx Semantic Digest

> AI-optimized semantic index. No narrative prose. Fork of yuliskov/SmartTube adding keyword-title filtering + channel blacklist to an Android TV YouTube client.

## 1. System Topology & Data Flow

```text
SplashActivity ──> MainApplication ──> BrowseActivity/BrowseFragment
   ├── SidebarService        (sections: Home/Search/Subs/.../BlockedChannels)
   ├── BrowsePresenter       (section -> VideoGroup grids, local mappings)
   └── DataSourcePresenter   (youtubeapi -> List<Video>)
            │
            ▼
      VideoGroup.add(video)  ◄── FILTER GATE (keyword + channel + watched)
            │
            ▼
   VideoGroupObjectAdapter / GridFragment -> Leanback UI -> PlaybackActivity

Settings path:  BrowsePresenter -> GeneralSettingsPresenter -> KeywordFilterManager
Context menu:   VideoMenuPresenter.appendBlockChannelButton -> BlockedChannelData
Persistence:    AppPrefs (profile-scoped)  |  SharedPreferences("blocked_words_prefs")
Backup:         BackupAndRestoreManager -> Utils.BACKUP_PREFS (includes blocked_words_prefs.xml)
```

## 2. Global Constraints & Architecture Patterns

- **Language / Build**: Java 8 source level (210 java files in `common`, 186 in `smarttubetv`); AGP 7.4.2, Gradle 7.5, JDK 17 to build, `minSdk 21` (stfdroid) / higher for ststable/stbeta.
- **Architecture**: MVP presenters + views. `common/` holds all shared logic and presenters; `smarttubetv/` holds TV UI (activities, fragments, presenters, adapters).
- **Hard Constraints (repo rules AGENTS.md/GEMINI.md)**: <400 LOC/file (300 soft), <60 LOC/fn (40 soft), max 5 params, max 3 nesting, zero emoji in UI, zero unverified `@Suppress` on Compose lints (project is XML/Leanback, not Compose). Kotlin used in 8 files only.
- **Flavors**: `stbeta` (org.smarttube.beta), `ststable` (org.smarttube.stable), `stfdroid` (app.smarttube.fdroid). ApplicationId base `app.smarttube`.
- **Submodules**: `SharedModules` and `MediaServiceCore` are git submodules and currently EMPTY on disk. Clean build requires `git submodule update --init --recursive`; `settings.gradle` applies `core_settings.gradle`/`constants.gradle` from them.
- **Target Distribution**: Android APK per-ABI (armeabi-v7a, arm64-v8a, x86, universal) via GitHub Actions CI (`assembleStbetaRelease`) + releases.

## 3. Module & Interface Skeleton

### `settings.gradle` (Role: build, Lines: 25)
- **Responsibility**: Includes app modules; resolves submodule roots with fallback (`../X` else `./X`); applies `core_settings.gradle` for SharedModules/MediaServiceCore/exoplayer.
- **Included modules**: `:smarttubetv :common :chatkit :leanbackassistant :leanback-1.0.0 :fragment-1.1.0 :filepicker-lib :doubletapplayerview :slidableactivity` + SharedModules/MediaServiceCore/exoplayer generated includes.

### `smarttubetv/build.gradle` (Role: app/build, Lines: 250)
- **Responsibility**: App module build. Version `32.08` (versionCode 2398). ABI splits + universal APK. Custom APK naming `SmartTube_<flavor>_<version>_<arch>.apk`. Flavor `stbeta` applies google-services + crashlytics when `google-services.json` present. Reads `keystore.properties` (root, gitignored) for `signingConfigs.release`, applied to both `release` and `debug` build types.
- **Consumers**: CI workflow, release process.

### `common/.../filter/KeywordFilterManager.java` (Role: domain/filter, Lines: 137)
- **Responsibility**: Singleton store + matcher for blocked title keywords.
- **Imports**: `android. mBlockedWords` (HashSet, normalized lowercase+trim). Prefs file `blocked_words_prefs`, key `blocked_words`. `MAX_KEYWORD_LENGTH=100`, `MAX_KEYWORDS_COUNT=200`.
- **Public Signatures**:
  ```java
  static synchronized KeywordFilterManager instance(Context context)
  synchronized void addWord(String word)      // normalize, cap length+count, save
  synchronized void removeWord(String word)   // normalize, remove, save
  synchronized Set<String> getWords()          // defensive copy
  synchronized boolean isBlocked(String title) // O(n) loop: normalizedTitle.contains(blocked)
  ```
- **Consumers**: `GeneralSettingsPresenter` (add/list/remove UI), `VideoGroup.isKeywordBlocked`.
- **Side Effects / I/O**: reads+writes `SharedPreferences("blocked_words_prefs")`.

### `common/.../filter/VideoFilter.java` (Role: domain/filter, Lines: 40)
- **Responsibility**: Batch helper to drop keyword-blocked videos from a list.
- **Public Signatures**:
  ```java
  static List<Video> filterBlocked(Context context, List<Video> videos)
  ```
- **Consumers**: NONE — dead code. Enforced filtering happens inline in `VideoGroup.add`.

### `common/.../prefs/BlockedChannelData.java` (Role: domain/prefs, Lines: 228)
- **Responsibility**: Profile-scoped persistence + membership for blacklisted channels.
- **Imports**: `AppPrefs.ProfileChangeListener`, `Helpers`, `Utils`, `android.util.Pair`, `java.util.{ArrayList,List,Map,Entry}`.
- **Types**:
  ```java
  private static class Channel { String channelId; String channelName;
    static Channel fromString(String specs); boolean equals(Object); String toString(); }
  public interface BlockedChannelListener { void onChanged(); }
  ```
  `Channel.equals`: if both names present compares names (`Helpers.equals`); else id branch — BUGGY, compares `Channel` object to `String` id (line ~62), id fallback never true.
- **State**: `List<Channel> mChannels`, key `blocked_channel_data` in `AppPrefs`. Delayed persist 10s (`Utils.postDelayed`), immediate via `persistNow()`.
- **Public Signatures**:
  ```java
  static BlockedChannelData instance(Context)
  void addChannel(String channelId, String channelName)       // prepend, persist, notify
  void removeChannel(String channelId, String channelName)
  boolean containsChannel(String channelId, String channelName)
  List<Pair<String,String>> getChannelIdsWithNames()
  int getChannelCount(); boolean isEmpty(); void clear()
  void persistNow()
  void addListener/removeListener(BlockedChannelListener)
  void onProfileChanged()  // restoreState()
  ```
- **Consumers**: `VideoMenuPresenter`, `BrowsePresenter.getBlockedChannels`, `VideoGroup.isChannelBlocked`, `Video.belongsToBlockedChannels`, `Utils`.
- **Side Effects / I/O**: AppPrefs profile data read/write.

### `common/.../app/models/data/VideoGroup.java` (Role: domain/model, Lines: 522)
- **Responsibility**: Ordered list of `Video` for one UI section; central insertion filter gate.
- **Filter Gate** (`add(int idx, Video)` line ~458):
  ```java
  if (video == null || video.isEmpty() || isChannelBlocked(video)
      || isKeywordBlocked(video) || isWatchedAndRecommended(video)) return;
  ```
- **Key private signatures**:
  ```java
  private boolean isChannelBlocked(Video video)  // skips chapters; BlockedChannelData.containsChannel
  private boolean isKeywordBlocked(Video video)  // skips chapters; KeywordFilterManager.isBlocked(title)
  private boolean isWatchedAndRecommended(Video) // only TYPE_SUGGESTIONS
  ```
- **Consumers**: `VideoGroupObjectAdapter`, `DeferredVideoGroupObjectAdapter`, grid fragments. Every list flowing through here is auto-filtered (Home/Search/Subs/Playlists/Channel uploads).

### `common/.../app/models/data/Video.java` (Role: domain/model, Lines: 978)
- **Responsibility**: Video/channel/playlist/header model.
- **Relevant signatures**: `boolean belongsToBlockedChannels()`, `String getChannelIdOrName()`, `belongsToGroup(long)`, `getTitle()`, `getAuthor()`, `sync(State)`.

### `common/.../app/presenters/settings/GeneralSettingsPresenter.java` (Role: presenter, Lines: 848)
- **Responsibility**: General settings tree incl. keyword filter entry.
- **Relevant signatures**:
  ```java
  private void appendKeywordFilterCategory(AppDialogPresenter)
  private void showKeywordFilterDialog() // add via SimpleEditDialog; list words; confirm remove
  ```
- **Consumers**: settings UI. Uses strings `settings_keyword_filter`, `keyword_filter_add/list/remove/confirm_remove`.

### `common/.../app/presenters/dialogs/menu/VideoMenuPresenter.java` (Role: presenter, Lines: 1048)
- **Responsibility**: Long-press context menu.
- **Relevant signatures**:
  ```java
  private void appendBlockChannelButton()  // toggles BlockedChannelData add/remove; ACTION_REMOVE
  private void showHideBlockedChannelsSection(BlockedChannelData) // enable/disable TYPE_BLOCKED_CHANNELS section
  ```
- **Strings**: `dialog_block_channel`, `dialog_unblock_channel`, `channel_blocked`, `channel_unblocked`.

### `common/.../app/presenters/BrowsePresenter.java` (Role: presenter, Lines: 1270)
- **Responsibility**: Builds sidebar sections + grid mappings.
- **Relevant signatures**:
  ```java
  private void initLocalGridMapping() // mLocalGridMappings.put(TYPE_BLOCKED_CHANNELS, this::getBlockedChannels)
  private List<Video> getBlockedChannels() // maps Pair<id,name> -> Video placeholders
  ```
- **Section def**: line ~204 `mSectionsMapping.put(TYPE_BLOCKED_CHANNELS, new BrowseSection(..., R.drawable.icon_blocked_channels, false))`.

### `common/.../app/presenters/dialogs/AppUpdatePresenter.java` (Role: presenter, Lines: 117)
- **Responsibility**: Update check + install flow. Background checks are silent (no sidebar entry, no dialog); only an explicit force-check shows the install dialog.
- **Public signatures**:
  ```java
  static AppUpdatePresenter instance(Context)
  static void unhold()
  void start(boolean forceCheck)
  void onUpdateFound(String versionName, List<String> changelog, String apkPath)
  void onUpdateError(Exception error)
  ```
- **Consumers**: `BootDialogPresenter` (boot background check), `SplashPresenter.checkForUpdates`, `AboutSettingsPresenter`/`AboutSimpleSettingsPresenter` (manual check).
- **Side Effects / I/O**: network manifest fetch via `AppUpdateChecker`; on force-check opens `AppDialogPresenter` install dialog. Background found-update now only calls `onFinish()`.

### `common/.../app/presenters/service/SidebarService.java` (Role: presenter, Lines: 331)
- **Responsibility**: Default sidebar sections; registers `R.string.header_blocked_channels -> MediaGroup.TYPE_BLOCKED_CHANNELS`.

### `common/.../utils/Utils.java` (Role: infra/util, Lines: 1308)
- **Responsibility**: misc helpers incl. backup allowlist.
- **Relevant**: `BACKUP_PREFS` includes `"blocked_words_prefs.xml"` (line ~116). `BACKUP_DIRS`, `KNOWN_PACKAGES`.

### `common/.../misc/BackupAndRestoreManager.java` (416) / `BackupAndRestoreHelper.java` (327) / `app/presenters/settings/BackupSettingsPresenter.java` (235)
- **Responsibility**: Export/import prefs + dirs listed in `Utils.BACKUP_PREFS`/`BACKUP_DIRS`. Channel blacklist rides in AppPrefs profile data.

### Resources
- `common/src/main/res/values/strings.xml`: `settings_keyword_filter`, `keyword_filter_add/remove/list/confirm_remove`, `dialog_block_channel`, `dialog_unblock_channel`, `confirm_block_channel`, `header_blocked_channels`, `channel_blocked/unblocked`, `msg_no_blocked_channels`. Translations across many locales under `values-*`.

### CI: `.github/workflows/CI.yml` (Lines: 118)
- **Trigger**: push to master, workflow_dispatch.
- **Steps**: checkout (submodules recursive) -> JDK 17 -> append `-nightly-<run>` to versionName -> optional keystore -> `./gradlew lintStbetaRelease` -> `clean assembleStbetaRelease` -> optional VirusTotal -> upload arm64/armv7/universal/x86 APKs.

### Release: `.github/workflows/release.yml` (Lines: 78)
- **Trigger**: push of tags matching `v*`.
- **Secrets**: `SIGNING_KEY` (base64 JKS), `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`.
- **Steps**: checkout (submodules recursive) -> JDK 17 -> write `keystore.properties` + decode `key.jks` -> `clean assembleStstableRelease` -> `sha256sum` -> `apksigner verify --print-certs` -> publish GitHub Release from `.github/RELEASE_NOTES.md` with all ststable release APKs + `SHA256SUMS.txt`.
- **Release notes body**: `.github/RELEASE_NOTES.md`.

## 4. Execution Lifecycle Trace

1. **Startup**: `SplashActivity` -> `MainApplication` -> `BrowseActivity` + `BrowseFragment`.
2. **Sidebar**: `SidebarService` defines default sections incl. `TYPE_BLOCKED_CHANNELS` (hidden until first block added).
3. **Data load**: `DataSourcePresenter` fetches `List<Video>` from youtubeapi -> wrapped into `VideoGroup`.
4. **Filtering (single gate)**: each `VideoGroup.add` runs `isChannelBlocked` + `isKeywordBlocked` + `isWatchedAndRecommended`; blocked items never enter the adapter.
5. **UI**: `VideoGroupObjectAdapter`/grid fragments render; playback via `PlaybackActivity`/`PlaybackFragment` (ExoPlayer-amzn).
6. **Keyword config**: Settings -> `GeneralSettingsPresenter.showKeywordFilterDialog` -> `KeywordFilterManager` persists.
7. **Channel config**: long-press card -> `VideoMenuPresenter.appendBlockChannelButton` -> `BlockedChannelData` persists + toggles section.
8. **Backup**: `BackupAndRestoreManager` exports/imports `blocked_words_prefs.xml` + AppPrefs (channel list inside).

## 5. Verification Commands

```bash
# Init submodules first (required for any gradle task)
git submodule update --init --recursive

# Lint (CI)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew lintStbetaRelease

# Build debug (README) / release (CI)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleStstableDebug
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew clean assembleStbetaRelease

# Requires local.properties: sdk.dir=<path to Android SDK>

# APK output
# smarttubetv/build/outputs/apk/ststable/debug/  |  .../stbeta/release/
```

## 6. Known Issues / Debt

- `KeywordFilterManager.isBlocked` is O(n) substring scan, not O(1) as README claims. HashSet is storage only.
- `VideoFilter.filterBlocked` is dead code (never referenced).
- `BlockedChannelData.Channel.equals` id-fallback branch compares `Channel` to `String` — always false; name-less blocked channels won't match by id.
- Keyword filter matches title only (not description/tags/channel).
- No unit tests for `KeywordFilterManager` or `BlockedChannelData` (Robolectric configured in `common`).
- `GeneralData.mIsOldUpdateNotificationsEnabled` (prefs index 43) is now unused after the sidebar-update removal; kept to avoid shifting positional prefs parsing.
- `SharedModules` / `MediaServiceCore` submodules are initialized via `git submodule update --init --recursive`.

## 7. Recent Iteration Changes

- **2026-10-05**: Removed sidebar "Update" entry. `AppUpdatePresenter.onUpdateFound` background path is now silent (was `pinUpdateSection` -> `BrowsePresenter.pinItem`); deleted `pinUpdateSection`, `createChangelog`, and unused imports. Collapsed the About settings 3-way notification radio (`sidebar_notification`/`dialog_notification`) to a single `check_updates_auto` switch. Manual `Check for updates` still shows the install dialog. `GeneralData.mIsOldUpdateNotificationsEnabled` left in place (positional prefs parsing) but now unused.
- **2026-10-05**: Initial `CODEBASE.md` generated from full-repo exploration (filter engine, persistence, UI, CI, module graph).