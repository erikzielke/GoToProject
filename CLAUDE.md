# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

**Go To Project** is an IntelliJ Platform plugin (Kotlin) for switching between open project windows with speed search. It can also list recent projects, jump back to the last focused project, and add a "Projects" tab to Search Everywhere. Project names show the current git branch and mark linked worktrees.

- Marketplace: https://plugins.jetbrains.com/plugin/7359-go-to-project
- Plugin ID: `org.github.erikzielke.gotoproject`
- Package root: `src/main/kotlin/org/github/erikzielke/gotoproject/`

## Toolchain

- JDK 21 (`.sdkmanrc` pins `21.0.9-jbr`; CI uses Temurin 21). Java and Kotlin both target JVM 21.
- Gradle wrapper with the IntelliJ Platform Gradle Plugin 2.x (`org.jetbrains.intellij.platform`), building against `intellijIdea("2025.3")`.
- The version comes from git tags (`gradle-git-version-calculator`, prefix `v`), so builds need git history and tags.
- Plugins and versions are declared inline in `build.gradle.kts`. There is no version catalog.

## Commands

```bash
./gradlew build            # compile + test
./gradlew test             # run tests only
./gradlew test --tests 'org.github.erikzielke.gotoproject.git.GitBranchResolverTest'
./gradlew runIde           # launch a sandbox IDE with the plugin installed
./gradlew buildPlugin      # produce distributable zip in build/distributions/

./gradlew spotlessCheck    # formatting check (ktlint)
./gradlew spotlessApply    # auto-fix formatting
./gradlew detektAll        # static analysis, config at config/detekt/detekt.yml
./gradlew koverVerify      # coverage gate (min 30% line coverage, see `kover {}` in build.gradle.kts)
```

CI (`.github/workflows/build.yml`) runs `spotlessCheck`, then `detektAll`, then `build`, then the Kover reports. Before you push, run at least `./gradlew spotlessApply detektAll build`.

## Architecture

All extension points are registered in `src/main/resources/META-INF/plugin.xml`. UI strings live in `src/main/resources/messages.properties`.

| File | Role |
|---|---|
| `GoToProjectApplicationComponent` | App-level `@Service` and `PersistentStateComponent`. It persists settings to `GoToProjectWindow.xml` and holds the in-memory `focusedBefore` and `lastFocusLost` projects. Get it via `GoToProjectApplicationComponent.instance`. |
| `GoToProjectWindowSettings` | Persisted settings bean: `isIncludeRecent`, `showTabInSearchEverywhere`, `openTabInSearchEverywhere`. |
| `GotoProjectApplicationConfigurable` | Settings UI page for those flags. |
| `GoToProject` | Main action, added to the `OpenProjectWindows` group. It shows a speed-search popup of open projects (last active first) and, if enabled, recent projects. When `openTabInSearchEverywhere` is set, it opens the Search Everywhere "Projects" tab instead. |
| `GoToProjectWindowAction` | Toggle action for one open project. It brings the project's frame to the front and restores the frame if minimized, except via the keyboard on macOS. |
| `GoToRecentProjectAction` | Wraps `ReopenProjectAction` so its label can include the branch. The delegate overwrites its own text in `update()`. |
| `GoToLastProject` | Focuses `focusedBefore`. Disabled when there is no previous project. |
| `focus/GoToProjectProjectManagerListener` | `ProjectActivity` + `ProjectManagerListener`. It attaches or removes a `ProjectWindowFocusListener` on each project frame and maintains the top-level `projects` list. |
| `focus/ProjectWindowFocusListener` | Tracks focus changes between windows to set `focusedBefore`. |
| `searcheverywhere/*` | Search Everywhere contributor, factory, and cell renderer for the "Projects" tab. Matching uses `NameUtil.buildMatcher("*$pattern")`, case-insensitive. |
| `git/GitBranchResolver` | Reads `.git/HEAD` directly, without depending on Git4Idea. It handles linked worktrees (a `.git` file containing `gitdir:`) and a detached HEAD (short hash). Results are cached for 5s per path. |

## Conventions

- Kotlin official code style, formatted by ktlint through Spotless (4-space indent, trailing commas, final newline). Don't format by hand; run `spotlessApply`.
- Fix detekt findings rather than suppressing them. Constants such as `CACHE_TTL_MILLIS` exist to satisfy the magic-number rule.
- Actions should be dumb-aware (`DumbAware`, `DumbAwareToggleAction`, or `isDumbAware() = true`) and should return `ActionUpdateThread.BGT` from `getActionUpdateThread()`.
- Don't add a dependency on the Git4Idea plugin. `plugin.xml` depends only on `com.intellij.modules.platform`.
- When adding an action, configurable, or extension, register it in `plugin.xml` and add any user-visible text to `messages.properties`.
- Tests are in `src/test/kotlin/...` and mirror the main package layout. Platform-dependent tests extend `BasePlatformTestCase`. Pure logic uses `kotlin-test` and `mockito-kotlin`.

## Release

`.github/workflows/publish.yml` is triggered manually with a channel input (default `alpha`). It runs `publishPlugin` using the `PUBLISH_TOKEN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, and `CERTIFICATE_CHAIN` secrets. Change notes are set in `patchPluginXml` in `build.gradle.kts`.
