# Trimmer — Fabric 1.21.11 client optimizer

Small, safe, client-only optimizations that deliberately do **not** overlap with Sodium, Lithium,
More Culling, ImmediatelyFast, Entity Culling, FerriteCore, ModernFix or Iris. Those mods already own
rendering, culling, game logic and memory layout; Trimmer only touches things none of them do.

| Feature | What it really does | Where it helps |
|---|---|---|
| Background throttle | Lowers the frame limit the game returns while the window is unfocused (default 15 FPS, then 5 FPS after 60 s). It only ever lowers the value, never raises it. | CPU/GPU/power/heat while alt-tabbed |
| Particle budget | Drops particles beyond a per-tick budget (default 500) and force-spawned particles farther than 80 blocks from the camera. Particles are cosmetic and client-only. | Lag spikes from explosions, fireworks, farm effects |
| Post-session cleanup | Runs one garbage collection ~5 s after you leave a world, while at the menu. | Heap shrinks after leaving big worlds; no effect during play |

It does **not** change chunk loading, entity or block rendering, or world data, and it makes no changes
to multiplayer traffic. Those areas are covered by the mods you already run, and changing them again
from here would risk conflicts.

Set `logStats=true` in `config/trimmer.properties` to log counters (frames throttled, particles dropped,
cleanups run) to `latest.log` every 60 s, so you can confirm each feature is active.

## Requirements to build

- **JDK 21** (e.g. Eclipse Temurin 21: https://adoptium.net). Check with `java -version`.
- Internet access on the first build (Gradle downloads Minecraft, Fabric Loom and Fabric API).
- Nothing else: the included Gradle wrapper downloads Gradle itself.

## Build the .jar

1. Unzip this project to a folder with no spaces if possible, e.g. `C:\dev\trimmer` or `~/dev/trimmer`.
2. Open a terminal **in that folder**.
3. Run:
   - Windows: `gradlew.bat build`
   - macOS / Linux: `chmod +x gradlew` then `./gradlew build`
4. The first run takes several minutes (it decompiles/remaps Minecraft). Later builds take seconds.
5. Your mod is at **`build/libs/trimmer-1.0.0.jar`**. Do not use `trimmer-1.0.0-sources.jar`.

## Build without installing anything (GitHub Actions)

1. Create a free GitHub repository and upload **all** project files, including the hidden
   `.github` folder. Dragging the folder into the browser often skips dot-folders, so use
   GitHub Desktop or `git push`, or create `.github/workflows/build.yml` by hand with
   "Add file > Create new file" and paste in its contents.
2. Open the repository's **Actions** tab. The build runs on every push; you can also
   choose "Build Trimmer mod" > **Run workflow**.
3. When the run shows a green check, open it and download **trimmer-fabric-1.21.11-jar**
   under *Artifacts*. GitHub wraps every artifact in a .zip download; unzip it and the
   `trimmer-1.0.0.jar` inside is the mod.
4. For a plain .jar download with no zip, push a tag such as `v1.0.0`. The jar is then
   attached to the repository's **Releases** page.

## Install

1. Install Fabric Loader 0.18.1 or newer for Minecraft 1.21.11 (https://fabricmc.net/use/installer/).
2. Put these in `.minecraft/mods`: `trimmer-1.0.0.jar`, Fabric API, plus your other mods.
3. Launch once. `config/trimmer.properties` is created; edit it and restart to tune.
4. Check `logs/latest.log` for a line starting `[Trimmer] Trimmer loaded`.

## If the build fails

- **"Could not find ... fabric-api" or loom version not found:** open https://fabricmc.net/develop/template/,
  choose 1.21.11, and copy `loader_version`, `loom_version` and `fabric_version` into `gradle.properties`.
- **"Unsupported class file major version" / wrong Java:** your `JAVA_HOME` is not JDK 21.
- **Gradle version error from Loom:** run `gradlew wrapper --gradle-version <version Loom asks for>`.
- **Compile error such as "cannot find symbol" in `src/main/java`:** Mojang class names occasionally move
  between versions. Send me the exact error and I will fix it.
- **Mixin warning in the log about an injection point:** that feature is skipped instead of crashing the
  game (the injectors use `require = 0`). Send me the log line.

## Uninstall / disable a feature

Delete the jar, or set the feature's flag to `false` in `config/trimmer.properties`.
If you use a streaming/recording source that captures the game while it is unfocused,
set `backgroundThrottle=false`. If another mod already throttles unfocused FPS
(such as Dynamic FPS), disable one of the two.
